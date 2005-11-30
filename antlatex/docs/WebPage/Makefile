all: index #manual

index:
	html_insert_text.pl "Skat/Schafkopf-Block -- Eine Anwendung für den Sharp SL 5500G (Zaurus)" navigation.html contents.html dtd_index_rahmen.html meta_info.html css.link >index.html
	tidy -wrap 0 -m -q -asxml index.html

clean:	
	rm -f index.html
	rm -f *~

ReadMe:
	sed -f striphtml.sed <contents.html >../ReadMe

manual:
	(cd refman; make pdf; make html;)
