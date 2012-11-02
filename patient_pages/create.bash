#!/bin/bash

c=1
while [ $c -le 19 ]
do
		filename="patient$c.html"
		echo $filename
		touch $filename
		echo "This is patient $c" > $filename
		cat div.html | tee -a $filename
		(( c++ ))
done

