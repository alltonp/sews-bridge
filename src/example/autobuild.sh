#!/usr/bin/env bash

# requires: brew install entr

rm -f client/Codec.elm
source ./sbt genElmCodec

#TIP: this might be a bit aggressive, perhaps should whack just our elm stuff ...
source client/clean.sh

cd client

ls `find . -name \*.elm -not -path \*elm-stuff*  -print` | entr sh -c 'clear; rm -f ../src/main/resources/js/elm.js; elm-make `find . -name \*.elm -not -path \*elm-stuff*  -print` --output ../src/main/resources/js/elm.js'