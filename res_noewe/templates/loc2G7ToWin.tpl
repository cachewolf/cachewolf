<#-- Export-Template for Export to 'G7ToWin'-->

<tmpl_par name="charset" value="ASCII">
<tmpl_par name="newline" value="CRLF">
<tmpl_par name="decsep" value=".">
<tmpl_par name="Out" value="*.loc">
<tmpl_par name="takeOnlyWp" value="main">

<?xml version="1.0"?><br/>
<loc version="1.0" src="EasyGPS"><br/>

<tmpl_loop cache_index>
   <waypoint><br/>
      <name id="<tmpl_var WAYPOINT>"><![CDATA[<tmpl_var NAME>]]></name><br/>
      <coord lat="<tmpl_var LAT>" lon="<tmpl_var LON>"/><br/>
      <type><tmpl_var GMTYPE></type><br/>
      <link><tmpl_var URL></link><br/>
   </waypoint>

   <tmpl_if ADDIS>
      <tmpl_loop ADDIS>
         <br/>
         <waypoint><br/>
            <name id="<tmpl_var WAYPOINT>"><![CDATA[<tmpl_var NAME>]]></name>
            <tmpl_if LAT><br/>
            <coord lat="<tmpl_var LAT>" lon="<tmpl_var LON>"/>
            <tmpl_else><br/>
            <coord lat="00.000001" lon="00.000001"/>
            </tmpl_if><br/>
            <type><tmpl_var TYPENAME></type><br/>
         </waypoint>
      </tmpl_loop>
   </tmpl_if>
   <br/>
   <br/>
</tmpl_loop>

</loc><br/>