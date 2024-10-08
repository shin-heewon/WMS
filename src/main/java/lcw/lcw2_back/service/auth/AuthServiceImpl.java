package lcw.lcw2_back.service.auth;

import lcw.lcw2_back.auth.JwtTokenProvider;
import lcw.lcw2_back.domain.user.User;
import lcw.lcw2_back.domain.userTest.Role;
import lcw.lcw2_back.dto.auth.LoginJwtResponse;
import lcw.lcw2_back.dto.auth.LoginRequest;
import lcw.lcw2_back.dto.auth.SignInRequest;
import lcw.lcw2_back.dto.user.UserDTO;
import lcw.lcw2_back.exception.auth.TokenExpirationException;
import lcw.lcw2_back.exception.auth.UserIdNotFoundException;
import lcw.lcw2_back.exception.auth.UserPasswordNotCorrectException;
import lcw.lcw2_back.exception.auth.UserStatusNotPermissionException;
import lcw.lcw2_back.global.Utils.PasswordEncoder;
import lcw.lcw2_back.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserInfo userInfo;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public LoginJwtResponse login(LoginRequest loginRequest) throws UserIdNotFoundException, UserPasswordNotCorrectException {
        if(loginRequest.getUserId().equals("admin")) {//admin은 걍 로그인 되게하자.
            userInfo.setUserName("임꺽정");
            userInfo.setUserBirth(LocalDate.of(1879,12,25));
            userInfo.setUserContact("01000000000");
            userInfo.setUserEmail("llld545@ssg.com");
            userInfo.setUserPosition(Role.GENERAL_MANAGER);
            userInfo.setUserId("admin");
            userInfo.setUserProfile(null);
            userInfo.setStorageId(null);
            return jwtTokenProvider.createLoginToken(loginRequest.getUserId(), Role.GENERAL_MANAGER);
        }
        User user = userMapper.selectUserById(loginRequest.getUserId());
        if(user==null) throw new UserIdNotFoundException("유저 이름을 찾을 수 없습니다.");

        String encryptedPassword = passwordEncoder.getSHA256EncryptedPassword(loginRequest.getPassword());
        User storageUser = userMapper.selectUserById(user.getUserId());

        if (!passwordEncoder.matches(encryptedPassword,storageUser.getUserPw())) {
            throw new UserPasswordNotCorrectException("비밀번호가 일치하지 않습니다.");
        }

        if(storageUser.getUserStatus()==null)
            throw new UserStatusNotPermissionException("회원 계정이 승인되지 않았습니다.");

        //UserInfo 에 login 한 유저정보 저장.
        userInfo.setUserName(user.getUserName());
        userInfo.setUserBirth(user.getUserBirth());
        userInfo.setUserContact(user.getUserContact());
        userInfo.setUserEmail(user.getUserEmail());
        userInfo.setUserPosition(user.getUserPosition());
        userInfo.setUserId(user.getUserId());
        userInfo.setUserProfile(user.getUserProfile());
        userInfo.setStorageId(user.getStorageId());

        return jwtTokenProvider.createLoginToken(user.getUserId(),user.getUserPosition());
    }

    @Override
    public void logout(String userId) {
        // 로그아웃 처리
       try{
        jwtTokenProvider.deleteTokenByUserId(userId);
       }catch (Exception e){
           System.out.println("토큰 레포지토리 오류...");
       }
    }

    @Override
    public void signIn(SignInRequest signInRequest) {
        String encryptedPassword = passwordEncoder.getSHA256EncryptedPassword(signInRequest.getUserPw());

        signInRequest.setUserPw(encryptedPassword);

        UserDTO userDTO = new UserDTO(signInRequest);

        User user = modelMapper.map(userDTO, User.class);

        userMapper.insertNewUser(user);
    }
    @Override
    public LoginJwtResponse reissueAccessToken(String refreshToken) {

        String userId = jwtTokenProvider.getUserId(refreshToken);
        System.out.println("리프레시 토큰으ㅏ로 userId가져오는것 성공:"+userId);
        Date expiration = jwtTokenProvider.getExpiration(refreshToken);
        //실제로는 user Table 에서 직접 가져오는것이 맞겠지...
        String role = jwtTokenProvider.getRole(refreshToken);

        // refreshToken과 DB에 저장된 refreshToken의 값이 일치하는지 확인
        if(!jwtTokenProvider.validateRefreshToken(refreshToken)){
            this.logout(userId);
            throw new TokenExpirationException("토큰의 유효기간 만료");
        }
        // 재발급
        String accessToken = jwtTokenProvider.createAccessToken(userId,Enum.valueOf(Role.class,role));
        System.out.println("재발급된 리프레시 토큰 : "+accessToken);

        return new LoginJwtResponse(accessToken, refreshToken);
    }

    @Override
    public User getLoginUserInfo(){
        return new User(userInfo.getUserId(),"비밀번호는 가져올 수 없습니다.",userInfo.getUserName(),userInfo.getUserPosition(),userInfo.getStorageId()
                ,userInfo.getUserBirth(), userInfo.getUserEmail(), userInfo.getUserContact(),"1", userInfo.getUserProfile());
    }
}
