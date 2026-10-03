package pierro.lmsPlatform.Repository.Auth;

/** Projection nhẹ cho JwtFilter: chỉ role và phiên bản token, không nạp cả entity User. */
public interface UserAuthInfo {
    String getRoleName();

    int getTokenVersion();
}
