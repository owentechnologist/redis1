package ex1;

import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;

public class Main {

    static String s = "";

    public static void main(String[] args) {
        Jedis r = new Jedis();

        r.del("shoppinglist");
        r.del("datatypesot");
        r.del("nodupes");
        r.del("apartments");
        r.del("bitkey");

        populateTasks(r);
        populateUsers(r);
        populateTaskWorkEntries(r);

        System.out.println("\t>> calling geo test");
        geo(r);

        System.out.println("\t>> calling lua test");
        lua(r);

        System.out.println("\t>> calling scan test");
        scan(r);

        System.out.println("\t\n calling bittest");
        //bitmap(r);

        System.out.println("\t>> calling strings test");
        //strings(r);
        System.out.println("\t>> calling lists test");
        lists(r);
        System.out.println("\t>> calling sets test");
        sets(r);
        System.out.println("\t>> calling sortedSets test");
        //sortedSet(r);
        System.out.println("\t>> calling hash test");
        //hash(r);
        System.out.println("\t>> calling expiration test");
        //expiration(r);
        System.out.println("\t>> calling hashedIndex test");
        hashedIndex(r);
        System.out.println("\t>> calling objectInspection test");
        objectInspection(r);
        System.out.println("\t>> calling facetedSearch test");
        facetedSearch(r);
    }

    public static void lua(Jedis r){
        String lscript ="-- this is a comment \n" +
        "local k1 = redis.call(\'get\', KEYS[1])"+"\n"+
        "local k2 = redis.call(\'get\', KEYS[2])"+"\n"+
        "if ARGV[1] == \"sum\" then"+"\n"+
        "   return k1+k2" +"\n"+
        "elseif ARGV[1] == \"max\" then"+"\n"+
        "   return math.max(k1,k2)"+"\n"+
        "else"+"\n"+
        "   return nil"+"\n"+
        "end\n";

        System.out.println(
                "\nlscript == \n"+lscript);

        String threed6 ="-- this is a comment \n" +
                "local d1 = redis.call(\'srandmember\', KEYS[1])"+"\n"+
                "local d2 = redis.call(\'srandmember\', KEYS[1])"+"\n"+
                "local d3 = redis.call(\'srandmember\', KEYS[1])"+"\n"+
                "return (d1+d2+d3)";
        System.out.println("\n threed6 == \n"+threed6);

        r.set("hits:homepage","2000");
        r.set("hits:loginpage","75");
        String lscriptHash = r.scriptLoad(lscript);
        List<String> KEYS = new ArrayList<String>();
        KEYS.add("hits:homepage");
        KEYS.add("hits:loginpage");
        List<String> ARGS = new ArrayList<String>();
        ARGS.add("sum");
        System.out.println("Result of calling sum with two keys: "+r.evalsha(lscriptHash,KEYS,ARGS));
        // comment to separate first function from 3d6 function:
        String l3d6Hash = r.scriptLoad(threed6);
        System.out.println(l3d6Hash);
        KEYS = new ArrayList<String>();
        ARGS = new ArrayList<String>();
        //List<Integer> IARGS = new ArrayList<Integer>();
        KEYS.add("d6");
        ARGS.add("-6");
        System.out.println("Result of calling l3d6: "+r.evalsha(l3d6Hash,KEYS,ARGS));

    }
    public static void geo(Jedis r){
        r.geoadd("pflugerville",-98,30,"startpoint");
        r.geoadd("pflugerville",-97.733330,30.266666,"78660");
        System.out.println(r.zrangeWithScores("pflugerville",0,-1));
        System.out.println("geohash for startpoint, 78660:  " +r.geohash("pflugerville","startpoint","78660"));
        System.out.println("distance between our points: "+r.geodist("pflugerville","78660","startpoint"));
        System.out.println("things within 180km of 78660:  "+r.georadiusByMember("pflugerville","78660",180,GeoUnit.KM).get(0).getMemberByString());

        r.zremrangeByRank("pflugerville",0,-1);
    }

/* bitwise and:  both 1's then 1 else 0
00101010 == 42
00010011 == 19
00000010 == 2
00111111 == 63
01011100 == 92  (64+16+8+4)
 */
    public static void bitmap(Jedis r){
        r.set("bitkey","\\0x10");
        System.out.println("bitcount says: "+r.bitcount("bitkey"));
        for(int x=0;x<64;x++){
            System.out.println("bitfield get u1 "+x+":  "+r.bitfield("bitkey","get","u1","#"+x));
        }
        System.out.println("get bitkey:  "+r.get("bitkey"));
        System.out.println("first byte: "+r.get("bitkey".getBytes())[0]);
        System.out.println("second byte: "+r.get("bitkey".getBytes())[1]);
        System.out.println("third byte: "+r.get("bitkey".getBytes())[2]);
       BitSet bitset = fromByteArrayReverse(r.get("bitkey".getBytes()));
        for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
            System.out.println("ID: " + i);
            if (i == Integer.MAX_VALUE) {
                break; // or (i+1) would overflow
            }
        }
    }

    private static BitSet fromByteArrayReverse(final byte[] bytes) {
        final BitSet bits = new BitSet();
        if (bytes != null) {
            for (int i = 0; i < bytes.length * 8; i++) {
                if ((bytes[i / 8] & (1 << (7 - (i % 8)))) != 0) {
                    bits.set(i);
                }
            }
        }
        return bits;
    }

    public static void scan(Jedis r) {
        ScanParams sp = new ScanParams();
        sp.count(100);
        sp.match("task:*");
        s="";
        String cursor = "0";
        do {
            ScanResult<String> sr = r.scan(cursor, sp);
            List<String> result = sr.getResult();
            if(result!=null && result.size() > 0){
                int x = result.size();
                while(x>0) {
                    x--;
                    String item = result.get(x)+" --> "+r.get(result.get(x));
                    s += "\t" +item;
                }
            }
        }while(! cursor.equals("0"));
        System.out.println("result of scan: "+"\n\t"+s);
        s="";
    }

    public  static void strings(Jedis r){
        //String: https://redis.io/commands#string
        r.set("y","a start");
        s = r.get("y");
        System.out.println(s);
        s="";
    }

    public static void expiration(Jedis r){
        //expiration test
        r.expire("y",2);
        try {
            Thread.sleep(1800);
            s = r.get("y");
            System.out.println(s);
            Thread.sleep(1800);
            s = r.get("y");
            System.out.println(s);
        }catch(InterruptedException ie){ie.printStackTrace();}
        s="";
    }

    public static void sets(Jedis r){
        //Sets https://redis.io/commands#set
        //no dupes
        r.sadd("nodupes","1");
        r.sadd("nodupes","2");
        r.sadd("nodupes","1");
        Set<String> lx = r.smembers("nodupes");
        for(String l :lx){
            s+="    "+l;
        }
        System.out.println(s);
        s="";
    }

    public static void sortedSet(Jedis r){
        //SortedSets https://redis.io/commands#sorted_set
        //scores
        r.zadd("apartments",100,"1 bedroom");
        r.zadd("apartments",200,"2 bedroom");
        r.zadd("apartments",300,"3 bedroom");
        r.zadd("apartments",300,"4 bedroom");
        Set<String> zx = r.zrevrange("apartments",0,-1);
        for(String l :zx){
            s+="    "+l;
        }
        System.out.println(s);
        s="";
        r.zremrangeByRank("apartments",0,r.zcard("apartments")-3); // remove bottom 2 values
        r.zremrangeByRank("apartments",r.zcard("apartments")-1,r.zcard("apartments")-1); // remove top 1 value
        zx = r.zrevrange("apartments",0,-1);
        for(String l :zx){
            s+="    "+l;
        }
        System.out.println(s);
        s="";

    }
    public static void hash(Jedis r){
        //Hash  https://redis.io/commands#hash
        java.util.HashMap<String,String> map = new HashMap();
        map.put("string","any data including sound and image files as bytes");
        r.hmset("datatypesot",map);
        System.out.println(r.hget("datatypesot","string"));
    }

    public static void lists(Jedis r){
        //List: https://redis.io/commands#list
        r.lpush("shoppinglist","apples","oranges","bananas","pumpkins","grapes");
        List<String> as = r.lrange("shoppinglist",0,-1);
        for(String l : as){
            s+="    "+l;
        }
        System.out.println(s);
        s="";
        r.ltrim("shoppinglist",0,1); // which ones to keep?
        as = r.lrange("shoppinglist",0,-1);
        for(String l : as){
            s+="    "+l;
        }
        System.out.println(s);
        s="";
    }

    public static void hashedIndex(Jedis r){

    }

    public static void objectInspection(Jedis r){

    }

    public static void facetedSearch(Jedis r){

    }

    public static void populateUsers(Jedis r){
        r.zadd("users:costPerMinute",1.5,"1:Bruce Martin");
        r.zadd("users:costPerMinute",2.22,"2:Angelica Smartypants");
        r.zadd("users:costPerMinute",.75,"3:Lucas Moranth");
    }

    public static void populateTasks(Jedis r){
        r.set("task:infra:1","configure redis server");
        r.set("task:code:1","define data model");
        r.set("task:test:1","test beta version");
        r.set("task:infra:2","establish GCP credentials");
    }

    public static void populateTaskWorkEntries(Jedis r){
        r.zadd("twe:minutes",15,"1:infra:1"); // user id = 1 spent 15 min on task 1
        r.zadd("twe:minutes",90,"1:infra:1");
        r.zadd("twe:minutes",45,"1:infra:2"); // user 1 spent 45 min on task 2
    }

}

