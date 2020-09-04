#!/bin/bash

sed 's/<h6>End Example<\/h6>/<\/div>/g' index.html | \
    sed 's/<h6>/<h6 class="exampleCode">/g' | \
    sed 's/<\/h6>/<\/h6><div  class="exampleCollapseSection">/g' | \
    sed 's/<\/h1>/<\/h1><div  class="collapseSection">/g'  | \
    sed 's/<h1>/<\/div><h1 class="collapsible">/g' > \
    index.test.html

cat template.mainpage.pre.html index.test.html template.mainpage.post.html > ../index.html

cat template.mainpage.pre.html index.html template.mainpage.post.html > ../index.html
