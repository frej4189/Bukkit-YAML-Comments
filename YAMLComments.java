import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class SaveWithComments {
	
	private final static DumperOptions yamlOptions = new DumperOptions();
    private final static Representer yamlRepresenter = new YamlRepresenter();
	
	public static void save(File file, YamlConfiguration config) throws IOException {
        Validate.notNull(file, "File cannot be null");

        Files.createParentDirs(file);

        String data = saveToString(config, file);
     

        Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);

        try {
            writer.write(data);
        } finally {
            writer.close();
        }
    }
	
	private static String saveToString(YamlConfiguration config, File file) {
        yamlOptions.setIndent(((YamlConfiguration) config).options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlOptions.setAllowUnicode(true);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        String header = config.options().header();
        Map<String, Object> values = config.getValues(false);
        
        StringBuilder sb = new StringBuilder();
        
        HashMap<Integer, String> comments = new HashMap<Integer, String>();
        ArrayList<Integer> whitespace = new ArrayList<Integer>();
        
        int lineAmount = 0;
        
        ArrayList<String> lines = new ArrayList<String>();
        
        try {
        	BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int count = 0;
            while((line = br.readLine()) != null) {
            	count++;
            	if (line.startsWith("#")) {comments.put(count, line); continue;}
            	if (line.trim().length() <= 0) {whitespace.add(count); continue;}
            }
            lineAmount = count;
            br.close();
        } catch (IOException e) {
			e.printStackTrace();
		}
        
        int valAmount = 0;
        
        for(int i = 0; i < lineAmount; i++) {
        	if(comments.containsKey(i + 1)) {lines.add(comments.get(i + 1)); continue;}
        	if(whitespace.contains(i + 1)) {lines.add(""); continue;}
        	
        	if(values.keySet().toArray().length >= valAmount) {
	        	String key = values.keySet().toArray()[valAmount].toString();
	        	lines.add(key + ": " + values.get(key));
	        	valAmount++;
        	} else Bukkit.getLogger().log(Level.WARNING, "Nothing matched line #" + i);
        }
        
        for(String line : lines) {
        	sb.append(line + "\n");
        }
        
        String dump = sb.toString();
        
        return header == null ? dump : header + dump;
    }
}
