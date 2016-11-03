package de.hska.lkit.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by jessedesaever on 24.10.16.
 */
@Controller
public class TimelineController {
    @RequestMapping(value = "/timeline")
    public String deliverTimelineTemplate() {
        return "timeline";
    }
}
