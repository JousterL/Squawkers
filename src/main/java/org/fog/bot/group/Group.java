package org.fog.bot.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlElement;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Data
@Slf4j
public class Group {
    @XmlElement(name = "groupName")
    private String name;
    @XmlElement(name = "groupMembers")
    private List<String> members;

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

    public String formatForSend(){
        StringBuilder memberBuidler = new StringBuilder();
        members.forEach(member -> memberBuidler.append("@").append(member));
        return memberBuidler.toString();
    }


}
