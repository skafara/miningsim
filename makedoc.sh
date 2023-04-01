#!/bin/bash
cd ./src
javadoc -encoding UTF-8 -sourcepath . -cp "." -d ../doc/javadoc -version -author *
cd ..
