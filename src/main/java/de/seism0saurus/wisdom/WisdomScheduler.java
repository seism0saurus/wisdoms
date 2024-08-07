package de.seism0saurus.wisdom;

import de.seism0saurus.wisdom.mastodon.StatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import social.bigbone.api.entity.Status;
import social.bigbone.api.exception.BigBoneRequestException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

/**
 * The WisdomScheduler is responsible for scheduling toots with a new wisdom picture.
 *
 * @author seism0saurus
 */
@Service
public class WisdomScheduler {

    /**
     * The {@link org.slf4j.Logger Logger} for this class.
     * The logger is used for logging as configured for the application.
     *
     * @see "src/main/ressources/logback.xml"
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(WisdomScheduler.class);

    /**
     * The {@link StatusRepository StatusRepository} of this class.
     * The repository is used to create new toots at mastodon.
     */
    private final StatusRepository repo;

    /**
     * The directory of the wisdom images and descriptions.
     * Can be configured in the <code>application.properties</code>.
     */
    @Value("${wisdoms.dir}")
    private String wisdomDirectory;

    /**
     * The sole constructor for this class.
     * The needed classes are {@link org.springframework.beans.factory.annotation.Autowired autowired} by Spring.
     *
     * @param statusRepository The  {@link StatusRepository StatusRepository} for mastodon.
     */
    public WisdomScheduler(@Autowired StatusRepository statusRepository) {
        this.repo = statusRepository;
    }

    /**
     * Schedules the posting of new wisdom pictures via mastodon toots.
     * postWisdom will be run according to the {@link org.springframework.scheduling.annotation.Scheduled Scheduled annotation}.
     * It generates a new post and creates a new toot on mastodon via the {@link StatusRepository StatusRepository}.
     * <p>
     * Exceptions are logged as errors and suppressed. No further error handling is applied.
     */
    @Scheduled(cron = "0 30 7 * * ?", zone = "Europe/Berlin")
    public void postWisdom() throws FileNotFoundException {
        LOGGER.info("Going to post new wisdom");

        String paddedWisdomNumber = getPaddedWisdomNumber();
        if (safeguardNumber(paddedWisdomNumber)) return;

        Map<String, Object> yaml = loadYaml(paddedWisdomNumber);
        if (safeguardYaml(yaml, paddedWisdomNumber)) return;

        File image = loadImage(paddedWisdomNumber);
        if (safeguardImage(image, paddedWisdomNumber)) return;

        LOGGER.info("Posting wisdom number " + paddedWisdomNumber);
        try {
            Status status = this.repo.postStatus(yaml, image);
            LOGGER.info("Wisdom successfully postet with id " + status.getId());
        } catch (BigBoneRequestException e) {
            LOGGER.error("An error occurred. Status code: " + e.getHttpStatusCode() + "; message: " + e.getMessage() + "; cause:" + e.getCause());
        }
    }

    private static boolean safeguardImage(File image, String paddedWisdomNumber) {
        if (image == null) {
            LOGGER.warn("Could not find any image for wisdom # " + paddedWisdomNumber);
            return true;
        }
        return false;
    }

    private static boolean safeguardYaml(Map<String, Object> yaml, String paddedWisdomNumber) {
        if (yaml == null || yaml.isEmpty()) {
            LOGGER.warn("Could not load any yaml for wisdom # " + paddedWisdomNumber);
            return true;
        }
        return false;
    }

    private boolean safeguardNumber(String paddedWisdomNumber) {
        if (paddedWisdomNumber == null) {
            LOGGER.warn("Could not find any wisdoms in " + wisdomDirectory);
            return true;
        }
        return false;
    }

    private String getPaddedWisdomNumber() {
        File wisdomDirectoryAsFile = new File(wisdomDirectory);
        File[] files = wisdomDirectoryAsFile.listFiles();
        if (files != null){
            List<File> wisdomList = Stream.of(files)
                    .filter(file -> file.getName().endsWith(".yaml") || file.getName().endsWith(".yml"))
                    .toList();
            Random rand = new Random();
            LOGGER.info("There are " + wisdomList.size() + " wisdoms configured");
            int imageNumber = rand.nextInt(wisdomList.size()) + 1;
            return String.format("%05d", imageNumber);
        }
        return null;
    }

    private Map<String, Object> loadYaml(final String paddedWisdomNumber) throws FileNotFoundException {
        String filename = wisdomDirectory + "/" + paddedWisdomNumber + ".yaml";
        Yaml yaml = new Yaml();
        File file = new File(filename);
        if (file.exists()){
            return yaml.load(new FileInputStream(file));
        }
        return Map.of();
    }

    private File loadImage(final String paddedWisdomNumber) {
        String filename = wisdomDirectory + "/" + paddedWisdomNumber + ".jpg";
        File jpg = new File(filename);
        if (jpg.exists()) {
            return jpg;
        }
        filename = wisdomDirectory + "/" + paddedWisdomNumber + ".jpeg";
        File jpeg = new File(filename);
        if (jpeg.exists()) {
            return jpeg;
        }
        filename = wisdomDirectory + "/" + paddedWisdomNumber + ".png";
        File png = new File(filename);
        if (png.exists()) {
            return png;
        }
        return null;
    }
}
