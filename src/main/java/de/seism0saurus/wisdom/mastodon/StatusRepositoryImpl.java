package de.seism0saurus.wisdom.mastodon;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import social.bigbone.MastodonClient;
import social.bigbone.api.entity.MediaAttachment;
import social.bigbone.api.entity.Status;
import social.bigbone.api.entity.data.Visibility;
import social.bigbone.api.exception.BigBoneRequestException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * The implementation of the {@link StatusRepository StatusRepository} to access, post and favourite toots as {@link social.bigbone.api.entity.Status Status}.
 *
 * @author seism0saurus
 */
@Service
public class StatusRepositoryImpl implements StatusRepository {

    /**
     * The {@link org.slf4j.Logger Logger} for this class.
     * The logger is used for logging as configured for the application.
     *
     * @see "src/main/ressources/logback.xml"
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(StatusRepositoryImpl.class);

    /**
     * The mastodon client is required to communicate with the configured mastodon instance.
     */
    private final MastodonClient client;


    /**
     * A list of greetings to greet all of your readers.
     * Add more to give the bot more options for its greeting.
     * Can be configured in the <code>application.properties</code>.
     */
    @Value("#{'${mastodon.text.greeting.en}'.split(';')}")
    private List<String> greetingsEnglish;

    /**
     * A list of greetings to greet all of your readers.
     * Add more to give the bot more options for its greeting.
     * Can be configured in the <code>application.properties</code>.
     */
    @Value("#{'${mastodon.text.greeting.de}'.split(';')}")
    private List<String> greetingsGerman;

    /**
     * The salutation to end the toots and wish the readers luck.
     * Can be configured in the <code>application.properties</code>.
     */
    @Value("#{'${mastodon.text.salutation.en}'.split(';')}")
    private List<String> salutationsEnglish;

    /**
     * The salutation to end the toots and wish the readers luck.
     * Can be configured in the <code>application.properties</code>.
     */
    @Value("#{'${mastodon.text.salutation.de}'.split(';')}")
    private List<String> salutationsGerman;

    /**
     * A list of hashtags.
     * Add more to give the bot more range. But keep it simple and precise.
     * Can be configured in the <code>application.properties</code>.
     */
    @Value("#{'${mastodon.text.tags}'.split(';')}")
    private List<String> tags;

    /**
     * The sole constructor for this class.
     * The needed classes are provided by Spring {@link org.springframework.beans.factory.annotation.Value Values}.
     *
     * @param instance    The mastodon instance for this repository. Can be configured in the <code>application.properties</code>.
     * @param accessToken The access token for this repository.
     *                    You get an access token on the instance of your bot at the {@link <a href="https://docs.joinmastodon.org/spec/oauth/#token">Token Endpoint</a>} of your bot's instance or in the GUI.
     *                    Can be configured in the <code>application.properties</code>.
     */
    public StatusRepositoryImpl(
            @Value(value = "${mastodon.instance}") String instance,
            @Value(value = "${mastodon.accessToken}") String accessToken) {
        this.client = new MastodonClient.Builder(instance)
                .accessToken(accessToken)
                .setReadTimeoutSeconds(240)
                .setReadTimeoutSeconds(240)
                .build();
        LOGGER.info("StatusInterfaceImpl for mastodon instance " + instance + " created");
    }

    public Status postStatus(Map<String, Object> yaml, final File uploadFile) throws BigBoneRequestException {

        // Get the description
        String description = (String) yaml.get("description");

        // Upload image to Mastodon
        final MediaAttachment uploadedFile = client.media().uploadMedia(uploadFile, "image/jpg", description).execute();
        final String mediaId = uploadedFile.getId();
        final List<String> mediaIds = List.of(mediaId);

        // Get the status text
        String statusText = createText(yaml);

        // Post the new status
        return client.statuses().postStatus(statusText, mediaIds, Visibility.PUBLIC, null, false, null, "de").execute();
    }


    @NotNull
    private String createText(final Map<String, Object> yaml) {
        Random rand = new Random();
        int greetingsIndex = rand.nextInt(greetingsGerman.size());
        int salutationsEnglishIndex = rand.nextInt(salutationsGerman.size());
        final String randomGreetingGerman = greetingsGerman.get(greetingsIndex);
        final String randomSalutationGerman = salutationsGerman.get(salutationsEnglishIndex);
        return randomGreetingGerman
                + "\n\n"
                + yaml.get("wisdom")
                + "\n\n"
                + randomSalutationGerman
                + "\n\n"
                + tags.stream()
                .map(tag -> "#" + tag)
                .collect(Collectors.joining(" "));
    }
}
