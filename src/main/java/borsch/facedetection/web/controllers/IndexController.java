package borsch.facedetection.web.controllers;

import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
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

        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
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

    @RequestMapping(value = "/person/check", method = RequestMethod.POST)
    public @ResponseBody boolean checkIfPersonExists(
            @RequestParam(name = "file") MultipartFile file
    ) {
        File localFile = new File(TEMP_FOR_REQUEST + "/" + file.getOriginalFilename());
        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        try {
            File[] persons = new File(ROOT_DIR).listFiles();
            if (persons == null) {
                return false;
            }

            for (File person : persons) {
                if (compareImages(localFile.getAbsolutePath(), person.getAbsolutePath())) {
                    knownPerson = true;

                    return true;
                }
            }

            return false;
        } finally {
            if (localFile.exists()) {
                localFile.setWritable(true);
                localFile.delete();
            }
        }
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

    @RequestMapping(value = "/person/enter", method = RequestMethod.GET)
    public @ResponseBody boolean canPersonEnter() {
        try {
            return knownPerson;
        } finally {
            knownPerson = false;
        }
    }

    private boolean compareImages(String path1, String path2) {
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        // first image
        Mat img1 = Imgcodecs.imread(path1, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Mat descriptors1 = new Mat();
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();

        detector.detect(img1, keypoints1);
        descriptor.compute(img1, keypoints1, descriptors1);

        // second image
        Mat img2 = Imgcodecs.imread(path2, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Mat descriptors2 = new Mat();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();

        detector.detect(img2, keypoints2);
        descriptor.compute(img2, keypoints2, descriptors2);

        // match these two keypoints sets
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptors1, descriptors2, matches);

        float total = 0;

        for (DMatch m : matches.toArray()) {
            // how to use these values to detect the similarity? They seem to be way off
            // all of these values are in range 50-80 which seems wrong to me
            total += m.distance;
        }

        float averageDistance = total / matches.total();

        System.out.println(path2 + ", distance score: " + averageDistance + ", commons in %: " + (100 - averageDistance) + "%");

        return averageDistance <= 50f;
    }

    private static boolean knownPerson = false;

    private String ROOT_DIR = System.getProperty("catalina.home") + "/temp/face_detection_saved";
    private String TEMP_FOR_REQUEST = System.getProperty("catalina.home") + "/temp/temp_requests";
}
