<tmpl_par name="charset" value="UTF8">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="Out" value="*.lmx">
<?xml version="1.0" encoding="UTF-8"?><br/>
<lm:lmx xmlns:lm="http://www.nokia.com/schemas/location/landmarks/1/0"<br/>
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"<br/>
        xsi:schemaLocation="http://www.nokia.com/schemas/location/landmarks/1/0/lmx.xsd"><br/>

  <lm:landmarkCollection><br/><br/>

    <tmpl_loop cache_index><br/><br/>

      <lm:landmark><br/>
          <lm:name><tmpl_if MAINWP><tmpl_var name=MAINWP>/</tmpl_if><tmpl_var name=WAYPOINT> <tmpl_var NAME></lm:name><br/>
          <lm:description>
<tmpl_if TYPE>Type: <tmpl_var TYPE>, </tmpl_if>
<tmpl_if SIZE>Size: <tmpl_var SIZE>, </tmpl_if>
<tmpl_if DIFFICULTY>Difficulty: <tmpl_var DIFFICULTY>, </tmpl_if>
<tmpl_if TERRAIN>Terrain: <tmpl_var TERRAIN>, </tmpl_if>
Hints: <tmpl_var DECRYPTEDHINTS>
          </lm:description><br/>
          <lm:coordinates><br/>
            <lm:latitude><tmpl_var name=LAT></lm:latitude><br/>
            <lm:longitude><tmpl_var name=LON></lm:longitude><br/>
          </lm:coordinates><br/>
          <lm:category><br/>
            <lm:name>Geocache</lm:name><br/>
          </lm:category><br/>
      </lm:landmark><br/><br/>

    </tmpl_loop><br/>

  </lm:landmarkCollection><br/><br/>

</lm:lmx>
