package borsch.facedetection.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
public class IndexController {

    public IndexController() {
        new File(ROOT_DIR).mkdirs();
        new File(TEMP_FOR_REQUEST).mkdirs();
    }

    @RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
    public String indexPage(){
        return "index/index";
    }

    @RequestMapping(value = "/person", method = RequestMethod.POST)
    public @ResponseBody boolean savePerson(
            @RequestParam(name = "file") MultipartFile file
    ) {
        File localFile = new File(ROOT_DIR + "/" + file.getOriginalFilename());
        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
        return true;
    }

    private String ROOT_DIR = System.getProperty("catalina.home") + "/temp/face_detection_saved";
    private String TEMP_FOR_REQUEST = System.getProperty("catalina.home") + "/temp/temp_requests";
}
