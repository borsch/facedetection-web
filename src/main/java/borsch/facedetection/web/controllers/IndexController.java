package borsch.facedetection.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

@Controller
public class IndexController {

    public IndexController() {
        new File(ROOT_DIR).mkdirs();
        new File(TEMP_FOR_REQUEST).mkdirs();
    }

    @RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
    public String indexPage(Model model){
        File[] persons = new File(ROOT_DIR).listFiles();

        if (persons != null) {
            model.addAttribute(
                    "persons",
                    Arrays.stream(persons)
                            .map(File::getName)
                            .collect(Collectors.toList())
            );
        }

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

    @RequestMapping(value = "/person/image", method = RequestMethod.GET)
    public void getPersonImage(
            @RequestParam("name") String name,
            HttpServletResponse response
    ) {
        File file = new File(ROOT_DIR + "/" + name);

        if (file.exists()) {
            try {
                response.getOutputStream().write(Files.readAllBytes(file.toPath()));
            } catch (IOException e) {
                // nothing to do
            }
        }
    }

    @RequestMapping(value = "/person/image", method = RequestMethod.DELETE)
    public @ResponseBody boolean deletePersonImage(
            @RequestParam("name") String name
    ) {
        File file = new File(ROOT_DIR + "/" + name);

        if (file.exists()) {
            file.setWritable(true);

            return file.delete();
        }

        return true;
    }

    private String ROOT_DIR = System.getProperty("catalina.home") + "/temp/face_detection_saved";
    private String TEMP_FOR_REQUEST = System.getProperty("catalina.home") + "/temp/temp_requests";
}
