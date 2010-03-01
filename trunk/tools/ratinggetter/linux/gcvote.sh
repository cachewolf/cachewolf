#!/bin/bash
# syntax: gcvote waypoint
# get the rating of the cache associated with waypoint
# waypoint is the code of the geocache staring with GCxxxx
# Rating from gcvote is returned as exit value
# by  Teleskopix / www.geoclub.de
# copyright: General Public License (GPL)
AVG=$(wget http://gcvote.de/getVotes.php?waypoints=$1 -O - -o /dev/null | grep voteAvg | sed -e 's/^.*voteAvg\=.\(.*\).\ voteCnt.*$/\1/g')
RET=$(echo "scale=0;$AVG*10" | bc | sed -e 's/\..*//g')
exit $(($RET))
