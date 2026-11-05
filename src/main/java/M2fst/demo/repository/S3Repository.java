package M2fst.demo.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class S3Repository {

    private static final Duration URL_VALIDITY = Duration.ofMinutes(10);

    private final S3Presigner s3Presigner;

    @Value("${s3.prefixe}")
    private String prefixe;

    /**
     * méthode permettant de récupérer l'url contenant le fichier correspondant à la clé
     *
     * @param bucket bucket dans lequel on cherche la key
     * @param key    clé correspondant à la ressource demandée
     * @return retourne l'url
     */
    public String getFileUrl(String bucket, String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(cleComplete(key))
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(URL_VALIDITY)
                .getObjectRequest(request)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toExternalForm();
    }

    /**
     * méthode permettant de récupérer l'url sur laquelle on va pouvoir upload notre ressource
     *
     * @param bucket bucket dans lequel on souhaite upload notre ressource
     * @param key    la clé qu'on veut associer à notre ressource
     * @return l'url sur laquelle le client peut upload son image
     */
    public String getUploadUrl(String bucket, String key) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(cleComplete(key))
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(URL_VALIDITY)
                .putObjectRequest(request)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toExternalForm();
    }

    /**
     * méthode qui permet de mettre le prefixe pour organiser proporement le s3
     *
     * @param key clé de la ressource
     * @return retourne la clé préfixée du dossier dans lequel le mettre
     */
    private String cleComplete(String key) {
        return prefixe + "/" + key;
    }
}
