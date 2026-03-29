package com.meradeya.app.dto.common;

import com.meradeya.app.exception.face.AppException;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

//Basically this class exists so we can both use in the code ProblemDetail provided by spring and 
// reference it in the api docs. Maybe it makes sense to have a separate class just for docs? 
@Schema(name = "ProblemDetail", description = "RFC 7807 Problem Details error response")
public class ExceptionDetail extends ProblemDetail {

  protected ExceptionDetail(int rawStatus) {
    super(rawStatus);
  }

  /**
   * Creates a {@link ExceptionDetail} pre-populated from the given {@link AppException}.
   * The title and detail are taken directly from the exception,
   * keeping handler code free of hard-coded strings.
   */
  public static ExceptionDetail forStatusAndException(HttpStatusCode status, AppException ex) {
    ExceptionDetail pd = new ExceptionDetail(status.value());
    pd.setTitle(ex.getTitle());
    pd.setDetail(ex.getMessage());
    return pd;
  }

  @Override
  @Schema(description = "URI reference identifying the problem type.", example = "about:blank")
  public URI getType() {
    return super.getType();
  }

  @Override
  @Schema(description = "Short, human-readable summary of the problem type.", example = "Email Already Exists")
  public String getTitle() {
    return super.getTitle();
  }

  @Override
  @Schema(description = "HTTP status code associated with this problem.", example = "409")
  public int getStatus() {
    return super.getStatus();
  }

  @Override
  @Schema(description = "Human-readable explanation specific to this occurrence of the problem.", example = "The email address is already registered.")
  public String getDetail() {
    return super.getDetail();
  }

  @Override
  @Schema(description = "URI reference identifying the specific occurrence of the problem.")
  public URI getInstance() {
    return super.getInstance();
  }

  @Override
  @Schema(description = "Additional problem-specific properties.")
  public Map<String, Object> getProperties() {
    return super.getProperties();
  }
}
