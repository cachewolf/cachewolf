package org.json;

import ewe.sys.*;
import ewe.sys.Double;

import ewe.util.Enumeration;
import ewe.util.Map;
import ewe.io.StringWriter;

import ewe.reflect.Array;
import ewe.reflect.Wrapper;


/**
 * Test class. This file is not formally a member of the org.json library.
 * It is just a casual test tool.
 */
public class Test {
	
    /**
     * Entry point.
     * @param args
     */
    public static void main(String args[]) {
        Vm.startEwe(args);

        Vm.debug( String.valueOf( Array.isArray( "foo" ) ) );
        
        double[] ia = { 1, 6, 95, 0, -200.6 };
        int x;
        Wrapper wrap = null;

        for( x = 0; x < Array.getLength( ia ); x++ )
        {
          wrap = Array.getElement( ia, x, wrap );
          Vm.debug( "[" + x + "] " + String.valueOf( wrap.toJavaWrapper() ) );
        }
  
        Enumeration it;
        JSONArray a;
        JSONObject j;
        Object o;
        String s;
        
/** 
 *  Obj is a typical class that implements JSONString. It also
 *  provides some beanie methods that can be used to 
 *  construct a JSONObject. It also demonstrates constructing
 *  a JSONObject with an array of names.
 */
        class Obj implements JSONString {
        	public String aString;
        	public double aNumber;
        	public boolean aBoolean;
        	
            public Obj(String string, double n, boolean b) {
                this.aString = string;
                this.aNumber = n;
                this.aBoolean = b;
            }
            
            public double getNumber() {
            	return this.aNumber;
            }
            
            public String getString() {
            	return this.aString;
            }
            
            public boolean isBoolean() {
            	return this.aBoolean;
            }
            
            public String getBENT() {
            	return "All uppercase key";
            }
            
            public String getX() {
            	return "x";
            }
            
            public String toJSONString() {
            	return "{" + JSONObject.quote(this.aString) + ":" + 
            	JSONObject.doubleToString(this.aNumber) + "}";
            }            
            public String toString() {
            	return this.getString() + " " + this.getNumber() + " " + 
            			this.isBoolean() + "." + this.getBENT() + " " + this.getX();
            }
        }      
        

    	Obj obj = new Obj("A beany object", 42, true);
        
        try {     
            s = "[0.1]";
            a = new JSONArray(s);
            Vm.debug(a.toString());
            Vm.debug("");
            
            j = new JSONObject();
            o = null;
            j.put("booga", o);
            j.put("wooga", JSONObject.NULL);
            Vm.debug(j.toString());
            Vm.debug("");
           
            j = new JSONObject();
            j.increment("two");
            j.increment("two");
            Vm.debug(j.toString());
            Vm.debug("");
            
            
            s = "{     \"list of lists\" : [         [1, 2, 3],         [4, 5, 6],     ] }";
            j = new JSONObject(s);
            Vm.debug(j.toString(4));
                    
            
            j = new JSONObject(obj);
            Vm.debug(j.toString());
            
            s = "{ \"entity\": { \"imageURL\": \"\", \"name\": \"IXXXXXXXXXXXXX\", \"id\": 12336, \"ratingCount\": null, \"averageRating\": null } }";
            j = new JSONObject(s);
            Vm.debug(j.toString(2));

        	int ar[] = {1, 2, 3};
        	JSONArray ja = new JSONArray(ar);
        	Vm.debug(ja.toString());
        	
        	String sa[] = {"aString", "aNumber", "aBoolean"};            
            j = new JSONObject(obj, sa);
            j.put("Testing JSONString interface", obj);
            Vm.debug(j.toString(4));          
            
            j = new JSONObject("{slashes: '///', closetag: '</script>', backslash:'\\\\', ei: {quotes: '\"\\''},eo: {a: '\"quoted\"', b:\"don't\"}, quotes: [\"'\", '\"']}");
            Vm.debug(j.toString(2));
            Vm.debug("");

            j = new JSONObject(
                "{foo: [true, false,9876543210,    0.0, 1.00000001,  1.000000000001, 1.00000000000000001," +
                " .00000000000000001, 2.00, 0.1, 2e100, -32,[],{}, \"string\"], " +
                "  to   : null, op : 'Good'," +
                "ten:10} postfix comment");
            j.put("String", "98.6");
            j.put("JSONObject", new JSONObject());
            j.put("JSONArray", new JSONArray());
            j.put("int", 57);
            j.put("double", 123456789012345678901234567890.);
            j.put("true", true);
            j.put("false", false);
            j.put("null", JSONObject.NULL);
            j.put("bool", "true");
            j.put("zero", -0.0);
            j.put("\\u2028", "\u2028");
            j.put("\\u2029", "\u2029");
            a = j.getJSONArray("foo");
            a.put(666);
            a.put(2001.99);
            a.put("so \"fine\".");
            a.put("so <fine>.");
            a.put(true);
            a.put(false);
            a.put(new JSONArray());
            a.put(new JSONObject());
            j.put("keys", JSONObject.getNames(j));
            Vm.debug(j.toString(4));

            Vm.debug("String: " + j.getDouble("String"));
            Vm.debug("  bool: " + j.getBoolean("bool"));
            Vm.debug("    to: " + j.getString("to"));
            Vm.debug("  true: " + j.getString("true"));
            Vm.debug("   foo: " + j.getJSONArray("foo"));
            Vm.debug("    op: " + j.getString("op"));
            Vm.debug("   ten: " + j.getInt("ten"));
            Vm.debug("  oops: " + j.optBoolean("oops"));

            j = new JSONObject("{nix: null, nux: false, null: 'null', 'Request-URI': '/', Method: 'GET', 'HTTP-Version': 'HTTP/1.0'}");
            Vm.debug(j.toString(2));
            Vm.debug("isNull: " + j.isNull("nix"));
            Vm.debug("   has: " + j.has("nix"));
            Vm.debug("");

            j = new JSONObject("{script: 'It is not allowed in HTML to send a close script tag in a string<script>because it confuses browsers</script>so we insert a backslash before the /'}");
            Vm.debug(j.toString());
            Vm.debug("");

            JSONTokener jt = new JSONTokener("{op:'test', to:'session', pre:1}{op:'test', to:'session', pre:2}");
            j = new JSONObject(jt);
            Vm.debug(j.toString());
            Vm.debug("pre: " + j.optInt("pre"));
            int i = jt.skipTo('{');
            Vm.debug(String.valueOf(i));
            j = new JSONObject(jt);
            Vm.debug(j.toString());
            Vm.debug("");

            a = new JSONArray(" [\"<escape>\", next is an implied null , , ok,] ");
            Vm.debug(a.toString());
            Vm.debug("");

            j = new JSONObject("{ fun => with non-standard forms ; forgiving => This package can be used to parse formats that are similar to but not stricting conforming to JSON; why=To make it easier to migrate existing data to JSON,one = [[1.00]]; uno=[[{1=>1}]];'+':+6e66 ;pluses=+++;empty = '' , 'double':0.666,true: TRUE, false: FALSE, null=NULL;[true] = [[!,@;*]]; string=>  o. k. ; \r oct=0666; hex=0x666; dec=666; o=0999; noh=0x0x}");
            Vm.debug(j.toString(4));
            Vm.debug("");
            if (j.getBoolean("true") && !j.getBoolean("false")) {
                Vm.debug("It's all good");
            }

            Vm.debug("");
            j = new JSONObject(j, new String[]{"dec", "oct", "hex", "missing"});
            Vm.debug(j.toString(4));

            Vm.debug("");
            j = new JSONObject("{string: \"98.6\", long: 2147483648, int: 2147483647, longer: 9223372036854775807, double: 9223372036854775808}");
            Vm.debug(j.toString(4));

            Vm.debug("\ngetInt");
            Vm.debug("int    " + j.getInt("int"));
            Vm.debug("long   " + j.getInt("long"));
            Vm.debug("longer " + j.getInt("longer"));
            //Vm.debug("double " + j.getInt("double"));
            //Vm.debug("string " + j.getInt("string"));

            Vm.debug("\ngetLong");
            Vm.debug("int    " + j.getLong("int"));
            Vm.debug("long   " + j.getLong("long"));
            Vm.debug("longer " + j.getLong("longer"));
            //Vm.debug("double " + j.getLong("double"));
            //Vm.debug("string " + j.getLong("string"));

            Vm.debug("\ngetDouble");
            Vm.debug("int    " + j.getDouble("int"));
            Vm.debug("long   " + j.getDouble("long"));
            Vm.debug("longer " + j.getDouble("longer"));
            Vm.debug("double " + j.getDouble("double"));
            Vm.debug("string " + j.getDouble("string"));

            j.put("good sized", 9223372036854775807L);
            Vm.debug(j.toString(4));

            a = new JSONArray("[2147483647, 2147483648, 9223372036854775807, 9223372036854775808]");
            Vm.debug(a.toString(4));

            Vm.debug("\nKeys: ");
            it = j.keys();
            while (it.hasMoreElements()) {
                s = (String)it.nextElement();
                Vm.debug(s + ": " + j.getString(s));
            }


            Vm.debug("\naccumulate: ");
            j = new JSONObject();
            j.accumulate("stooge", "Curly");
            j.accumulate("stooge", "Larry");
            j.accumulate("stooge", "Moe");
            a = j.getJSONArray("stooge");
            a.put(5, "Shemp");
            Vm.debug(j.toString(4));

            Map m = null;
            
            j = new JSONObject(m);
            j.append("stooge", "Joe DeRita");
            j.append("stooge", "Shemp");
            j.accumulate("stooges", "Curly");
            j.accumulate("stooges", "Larry");
            j.accumulate("stooges", "Moe");
            j.accumulate("stoogearray", j.get("stooges"));
            j.put("map", m);
            Vm.debug(j.toString(4));
            
            s = "{plist=Apple; AnimalSmells = { pig = piggish; lamb = lambish; worm = wormy; }; AnimalSounds = { pig = oink; lamb = baa; worm = baa;  Lisa = \"Why is the worm talking like a lamb?\" } ; AnimalColors = { pig = pink; lamb = black; worm = pink; } } "; 
            j = new JSONObject(s);
            Vm.debug(j.toString(4));
            
            s = " (\"San Francisco\", \"New York\", \"Seoul\", \"London\", \"Seattle\", \"Shanghai\")";
            a = new JSONArray(s);
            Vm.debug(a.toString());
            
            
            Vm.debug("\nTesting Exceptions: ");

            Vm.debug(">> Exception: ");
            try {
                a = new JSONArray("[\n\r\n\r}");
                Vm.debug(a.toString());
            } catch (Exception e) {
                Vm.debug(e.toString());
            }
            
            Vm.debug(">> Exception: ");
            try {
                a = new JSONArray("<\n\r\n\r      ");
                Vm.debug(a.toString());
            } catch (Exception e) {
                Vm.debug(e.toString());
            }
            
            Vm.debug(">> Exception: ");
            try {
                a = new JSONArray();
                a.put(Double.NEGATIVE_INFINITY);
                a.put(Double.NAN);
                Vm.debug(a.toString());
            } catch (Exception e) {
                Vm.debug(e.toString());
            }
            Vm.debug(">> Exception: ");
            try {
                Vm.debug(String.valueOf(j.getDouble("stooge")));
            } catch (Exception e) {
                Vm.debug(e.toString());
            }
            Vm.debug(">> Exception: ");
            try {
                Vm.debug(String.valueOf(j.getDouble("howard")));
            } catch (Exception e) {
                Vm.debug(e.toString());
            }
            Vm.debug(">> Exception: ");
            try {
                Vm.debug(j.put(null, "howard").toString());
            } catch (Exception e) {
                Vm.debug(e.toString());
            }
            Vm.debug(">> Exception: ");
            try {
                Vm.debug(String.valueOf(a.getDouble(0)));
            } catch (Exception e) {
                Vm.debug(e.toString());
            }
            Vm.debug(">> Exception: ");
            try {
                Vm.debug(a.get(-1).toString());
            } catch (Exception e) {
                Vm.debug(e.toString());
            }
            Vm.debug(">> Exception: ");
            try {
                Vm.debug(a.put(Double.NAN).toString());
            } catch (Exception e) {
                Vm.debug(e.toString());
            }
            Vm.debug(">> Exception: ");
            try {            	
            	ja = new JSONArray(new Object());
            	Vm.debug(ja.toString());
            } catch (Exception e) {
            	Vm.debug(e.toString());
            }

            Vm.debug(">> Exception: ");
            try {            	
            	s = "[)";
            	a = new JSONArray(s);
            	Vm.debug(a.toString());
            } catch (Exception e) {
            	Vm.debug(e.toString());
            }

            Vm.debug(">> Exception: ");
            try {            	
                s = "{\"koda\": true, \"koda\": true}";
                j = new JSONObject(s);
                Vm.debug(j.toString(4));
            } catch (Exception e) {
            	Vm.debug(e.toString());
            }

        } catch (Exception e) {
            Vm.debug( "Exception caught:");
            e.printStackTrace();
        }

      Vm.exit(0);
    }
}
