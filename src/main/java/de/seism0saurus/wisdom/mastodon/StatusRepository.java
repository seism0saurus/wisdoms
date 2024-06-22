package de.seism0saurus.wisdom.mastodon;

import social.bigbone.api.entity.Status;
import social.bigbone.api.exception.BigBoneRequestException;

import java.io.File;
import java.util.Map;

/**
 * The repository to access and manipulate {@link social.bigbone.api.entity.Notification Notifications}.
 *
 * @author faultierflora
 */
public interface StatusRepository {

    /**
     * Creates a new toot on mastodon.
     * The toot will be public, in english and without sensitivity warning or spoiler text.
     *
     * @param yaml The text of the new toot.
     * @param file The file of the image to upload
     * @return The newly posted {@link Status Status}.
     * @throws BigBoneRequestException Throws an exception,
     *                                 if there is a communication error with the configured mastodon instance or the contend is invalid.
     *                                 E.g. it could be to long.
     * @see <a href="https://docs.joinmastodon.org/methods/statuses/#create">Mastodon API Post a new status</a>
     * @see social.bigbone.api.method.StatusMethods#postStatus
     */
    Status postStatus(Map<String, Object> yaml, final File file) throws BigBoneRequestException;
}
