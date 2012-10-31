#!/bin/bash

c=1
while [ $c -le 2 ]
do
		filename="patient$c.html"
		echo $filename
		touch $filename
		echo "This is patient $c" > $filename
		(( c++ ))
done

