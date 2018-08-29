package org.fog.bot.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Data
@Slf4j
public class Group {

    @JsonProperty("groupName")
    private String name;
    @JsonProperty("groupMembers")
    private List<String> members;
    @JsonProperty("py/object")
    private String pythonClass;

    public Group(){}

    public Group(String name){
        this.name = name;
    }

    public Group(String name, List<String> members){
        this.name = name;
        this.members = members;
    }

    public static Group parseGroupFile(String gName) throws IOException{
        ObjectMapper om = new ObjectMapper();
        try {
            return om.readValue(new File(gName+".grp"), Group.class);
        } catch (IOException e) {
            log.error("Failed to load group file for {}", gName);
            throw e;
        }

    }

    public static void saveGroupFile(Group g) throws IOException{
        ObjectMapper om = new ObjectMapper();
        try {
            om.writeValue(new File(g.getName()+".grp"),g);
        } catch (IOException e) {
            log.error("Failed to write group file for {}", g.getName());
            throw e;
        }
    }

    public static void deleteGroupFile(String gName) throws IOException {
        if(!new File(gName+".grp").delete()){
            throw new IOException("Failed to delete file");
        }
    }

    public String formatForSend(){
        StringBuilder memberBuidler = new StringBuilder();
        members.forEach(member -> memberBuidler.append("@").append(member));
        return memberBuidler.toString();
    }


}
