# Stage 1 - Download
Pulled the dataset from [https://www.gov.pl/web/mswia/baza-jst](https://www.gov.pl/web/mswia/baza-jst) as an Excel file. *Note: This file is exempt from the CC license that otherwise applies to the rest of the dataset, as I don't own it in any way (it's literally just copy-pasted here for reference). I'm PRETTY SURE it's simply public-domain (given how, well, you can't really copyright a name; trademark it at most)~~, otherwise I'm absolutely screwed, as that'd make the CC license applied to everything else somewhat flakey~~.*

# Stage 2 - Extraction
Take the content of columns `B C D F G I J` and copy onto a new sheet. Then, save that sheet as CSV.

# Stage 3 - Replacements
All replacements were recorded, in the order of occurrence, in the `replacements.txt` file. Replacement was done with casing ignored unless otherwise noted. Every `$` is a whitespace (it looks like an S, which stands for Space), every `x` in replacement target means „remove that” (x isn't used in Polish, so it's a reasonable marker), and every `\` is a newline (it stands for `\n`, which is a common marker for NLs). Also, comments are supported. The whole process can be split into following stages (each one separated with an extra newline in the file):
## Fixing Polish Gov't's errors in the dataset
Ie. removing any trailing spaces.
## Dealing with street names
Replaced any shortened prefixes with their full counterparts (eg. `ul.` -> `ulica`, ie. `str.` -> `street`). After that, all Roman numerals were stripped by relying on the fact that `X` and `V` aren't used in Poland, and by running the `I\` sequence until it wasn't finding anything anymore (so that any multi-I sequences, like `III` are removed). Then, all normal numbers were also removed.
## Fixing errors in the dataset (again - tho some (not all) were a result of my action this time) and prepping for final processing.
First of all, while doing other stuff I noticed (and then remembered seeing that in Excel, too) that there are some random newlines in some places, so I removed those. Then, the trailing-spaces-deletion was done again to account for any streets that had numbers at the end. More processing came next: Some streets were called *X*-lecia (*X* Year's Celebration). After the numbers were stripped, we were left with a nonsense expression „-lecia” - these were changed to „Lecie” (ie. Celebration of the Year - and yes, this is different than Year's Celebration, as the latter in nonsensical in Polish without any number (or number-like, eg. „Kilku” meaning „A small number”) prefix). And while coming up with ideas on how to deal with that, I decided to take care of some extra stuff related to `-`, and also to take care of any remaining `.`'s (that most likely come from initials - and I had to replace them spaces to account for everyone who forgot a space after the `.` in the dataset). Finally, any double-spaces (that either got caused by replacing `.` with spaces or by removing numbers-that-were-like-words) had to be removed. I thought I was done at that point, but I then noticed some lines that had a space at the beginning, and also some `"` symbols, so I had to fix them. With all that taken care of, I could replace spaces with newlines, to prepare for the last step...
## Dealing with transformed adjectives (and some other stupid Polish grammar concepts)
It's best to illustrate this with an example. Let's say you have a 2 cities called Gorzów and a region called Wielkopolska (one of which is in this region). Now, you can't have 2 cities of the same name, so you rename one to Gorzów of Wielkopolska. This is cool'n'all, *but*... It's not possible in Poland. There is no „of” in Polish. Instead, you use Wielkopolska as an adjective. Thankfully, Polish (just like English) lets you use nouns as adjectives, so you have your beautiful „Wielkopolska Gorzów” and we're all happy! *EXCEPT* it's not that easy. First of all, *in this case* the adjective goes after the word (which is thankfully irrelevant for this dataset because it only cares about at individual words). Second of all, it's not „Gorzów Wielkopolska”, but „Gorzów Wielkopolski”. Unlike English, where you can *just* use nouns as adjectives and it's all context-based (eg. you can have a buffalo buffalo, and it means that your (noun) buffalo has the characteristics of a buffalo, ie. is (adjective) buffalo), Polish requires you to change the ending of a word in some way to signal that it's an adjective. There aren't really any rules for doing that, tbh, it's more a combination of having it learned by immersion in the language and „vibeing it out” as you go. It is, however, very clear, when something when something „feels like” an adjective. This is a problem for this dataset because there are a lot of places that include adjectives in them (the fact that I included columns C and D from the Excel sheet probably doesn't help because they were ALL adjectives) and the Markov chain may pick up on some low-level patterns, and then only generate places that sound like adjectives (which - although do happen - are certainly very rare and (especially with the types of adjectives that are in the dataset, ie. those ending with `ki`) tend to sound a bit unnatural). All the steps specified here are my best attempts at breaking this adjective-ish feeling. The order of application is important.

# Stage 4 - cleanup
The cleanup begins with removing the trailing newline. And also any duplicated newlines because despite my best efforts to purge them, 10 still somehow made it in. Then, everything gets pasted to https://phrasefix.com/tools/remove-duplicate-lines/ to remove duplicates (duh). Then, everything is pasted back into the file and some final replacements are done:
* newline+` `+newline is replaced with only a single newline
* ` `+newline is replaced with just a newline
* newline+` `  is replaced with just a newline

...because ` ` is *not* a space, so it wasn't registered before. Then another round of deduplication (because having those extra characters may have messed with the site's ability to detect duplicates - and it indeed did, because after pasting it again, the line count fell from 4721 to 4718) **and we're DONE!** The dataset got cleaned up from 29235 to 4718 entries. This is now usable on https://www.samcodes.co.uk/project/markov-namegen/

# Stage 5 - SKIE!
*Done, huh? Oh, had I wished I were...* After testing, it revealed a bias towards `-cach`, `-ach` and `-u` and `-ska`. Which is not good, because the 3 first ones are common suffixes for *being in places* not *places themselves*, while the last one is common for street names, not place names. This was corrected with the following replacement chain (same syntax as in Stage 3):
```
cach\ ce\
ku\ ek\
ńcu\ niec\
źcu\ ziec\
wcu\ wiec\
cu\ ec\
u\ \
ska\ \ #Only below line 3880 (this is where street-names start, so you can just spam the „replace 1” button) - all 47 entires above needed special care
```
There was no good way to replace `-ach` because sometimes `i` would fit, while `y` would be better in other cases (and some really didn't need replacing (they sounded town-like even with the `ach`) - and then there were Special Snowflakes of `e`, `a` and nothing), but there were only 62 matches, so I fixed them by hand. Because I *did* take some time and I didn't want to loose it, I made a backup (`stake5_manual.csv`) before another round of flattening. Speaking of which: This time, I didn't jump straight to https://phrasefix.com/tools/remove-duplicate-lines/ but first passed it through https://phrasefix.com/tools/capitalize-words/ to remove any words that were same-but-with-different-casing. That brought me down to a nice, computer-friendly `4096` lines. It's so nice, in fact, that I decided to leave in some template data that some computer-impaired civil servant left in, ie. `Nazwa_samorząd` (*originally was „samorządu”), `Nazwa_urzędu_jst` and a random em-dash (that presumably signals „Bro, idk how my city is called!”). It's also very funny and small enough (3 entires total) to likely drown in the Markov chain and not do any harm.

# Stage 6 - **WHY???**
Replaced `(jędrzejowska)` and `(konecka)` with `Jędrzejowska` and `Konecko` because some fucktard at the government put `()` in their dataset for some reason (and `(konecka)` especially had enough of a pull-force (due to 2 other similar places, „Koneck” and „Konecka”) to actually get `(` placed in the generated name list, so I had to clean it up).

# Stage 7 - Observations
After messing around (or, really, have [Midnight](https://github.com/Midnight-SP) mess around) with this generator in prod for a while, we noticed a major issue, ~~ie. the proximity resolver was absolutely fucked, but that's [already fixed](https://github.com/GuzioMG/LibVilNam/commit/ced24f36f6cc1337674a6497cb518f67006c1e07) and also irrelevant to this dataset~~ *there was a strong bias towards `cka` and `ow` endings*. Polish uses `ów` instead of `ow` 99,999% of the time, so I patched that up by doing `ow\ ów\` (again, same syntax as always). Then came the `cka`, which is, uhh... Complicated. Let's just say that about 70% of the time, when it generated, it didn't sound right. It felt more like a street name (because it kinda *was*). So it has to be purged from the set, too. Done the following:
```
icka\ iska\
tecka\ czecko\
recka\ rzecko\
żecka\ żecko\
ecka\ isko\
iisko\ isko\ #Previous step made some „i” doubled
cka\ czki\
```

# Stage 8 - RIP
I have bad news. After `diff`ing Stage 6 to 7, to see the extent of my changes, I noticed some doubled names. Which means that a yet another round of de-duping will be needed. Which means that we loose the nice'n'round 4096 names. Nooo! And now that there's no nice-and-rounding reason to keep it, we also loose the funny broken names (`Nazwa_samorząd` and `Nazwa_urzędu_jst`). NOO!!! At least there is *also* no reason to keep those short-broken words, too. But that requires some extra action first...

# Stage 9 - *The Pancakeing*
Every newline was converted to a space. Not only is this something that I have to do, anyway (to paste it to the config), but also I need this to do some regexing. Said regex was ` . ` and ` .. `, to get rid of all (they were replaced with spaces) the random prefixes/suffixes left after human-names were stripped.

# Stage 10 - ...Nevermind!
Undone newline-to-space. This was done purely to make diffing possible; the actual dataset is the same, and the file that was actually pasted is `stage9.csv`.