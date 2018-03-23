package org.fog.bot.group;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class Groups {
    private List<String> activeGroups;

}
