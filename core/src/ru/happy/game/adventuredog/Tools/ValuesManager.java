package ru.happy.game.adventuredog.Tools;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.happy.game.adventuredog.MainGDX;

public class ValuesManager {
    private final int RAND = -9999;
    private final Map<String,String[]> strings;
    private final ArrayList<String> buffer;

    public ValuesManager(){
        buffer = new ArrayList<>();
        strings = new HashMap<>();
        for (String i : AssetsTool.encodePlatform(AssetsTool.readFile("menu/values.bin")).split("\n\n")) {
            strings.put(i.substring(i.indexOf("[") + 1, i.indexOf("]")), i.substring(i.indexOf("\n") + 1).split("\n"));
        }
    }

    public String getString(String key, String def, Integer id) {
        String value;
        if (strings.containsKey(key)){
            int i = strings.get(key).length;
            if (id == RAND)
                value = strings.get(key)[MathUtils.random(strings.get(key).length-1)];
            else if (id >= 0 && id < i)
                value = strings.get(key)[id];
            else if (id < 0 && Math.abs(id)<=i)
                value = strings.get(key)[i+id];
            else
                value = def;
        } else {
            value = def;
        }
        return value;
    }
    public String getString(String key, String def) {
        return getString(key,def,0);
    }
    public String getString(String key, Integer id) {
        return getString(key,"NO VALUE",id);
    }
    public String getString(String key) {
        return getString(key,0);
    }
    public String getRandString(String key) {
        return getString(key,RAND);
    }

    public String[] getStrings(String key, String[] def) {
        return strings.containsKey(key)?strings.get(key):def;
    }
    public String[] getStrings(String key) {
        return getStrings(key,new String[]{"NO VALUE"});
    }

    public Integer getInteger(String key, Integer def, Integer id) {
        try {
            return Integer.parseInt(getString(key,""+def,id));
        } catch (NumberFormatException e){
            MainGDX.write(e.getLocalizedMessage());
        }
        return -1;
    }
    public Integer getInteger(String key, Integer def) {
        return getInteger(key,def,0);
    }
    public Integer getInteger(String key) {
        return getInteger(key,0);
    }
    public Integer getRandInteger(String key) {
        return getInteger(key,0,RAND);
    }
    public String find(String key, String param){
        buffer.clear();
        for (String s: getStrings(key))
            if (s.contains("["+param+"]"))
                buffer.add(s.replace("["+param+"]",""));
        return buffer.size()>0?buffer.get(MathUtils.random(buffer.size()-1)):"NO STRING";
    }
    public String findWithParams(String key, String find_param, String... params){
        String s = find(key,find_param);
        for (int i = 0; i < params.length; i++)
            s = s.replace("{"+i+"}",params[i]);
        return s;
    }
    public String getStringWithParams(String key, String... params){
        String s = getRandString(key);
        for (int i = 0; i < params.length; i++)
            s = s.replace("{"+i+"}",params[i]);
        return s;
    }
}
