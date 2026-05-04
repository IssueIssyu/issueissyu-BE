package issueissyu.backend.ci;

import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * CI / 로컬에서 .env 의 AWS_* 로 S3 접근이 되는지 검증 (head → put → get → delete).
 * {@code ./gradlew s3Smoke -x test}
 */
public final class S3SmokeMain {

	public static void main(String[] args) throws IOException {
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		String ak = pick(dotenv, "AWS_ACCESS_KEY");
		String sk = pick(dotenv, "AWS_SECRET_KEY");
		String region = pick(dotenv, "AWS_REGION");
		String bucket = pick(dotenv, "AWS_BUCKET");
		if (ak == null || sk == null || region == null || bucket == null) {
			throw new IllegalStateException("Missing env: AWS_ACCESS_KEY, AWS_SECRET_KEY, AWS_REGION, AWS_BUCKET");
		}

		try (S3Client s3 = S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ak, sk)))
				.build()) {

			s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());

			String key = "ci-smoke/" + UUID.randomUUID().toString().replace("-", "") + ".txt";
			byte[] body = "ci-smoke-test".getBytes();

			s3.putObject(
					PutObjectRequest.builder().bucket(bucket).key(key).contentType("text/plain").build(),
					RequestBody.fromBytes(body));

			byte[] read = s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build()).readAllBytes();
			if (!Arrays.equals(read, body)) {
				throw new IllegalStateException("S3 get object body mismatch");
			}

			s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
		}
	}

	private static String pick(Dotenv dotenv, String key) {
		String v = dotenv.get(key);
		if (v != null && !v.isBlank()) {
			return v;
		}
		v = System.getenv(key);
		if (v != null && !v.isBlank()) {
			return v;
		}
		return null;
	}
}
