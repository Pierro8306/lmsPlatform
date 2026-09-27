package pierro.lmsPlatform.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pierro.lmsPlatform.DTO.Request.Auth.CreateUserDto;
import pierro.lmsPlatform.DTO.Response.UserResposeDto;
import pierro.lmsPlatform.Entity.Auth.Role;
import pierro.lmsPlatform.Entity.Auth.User;
import pierro.lmsPlatform.Entity.Auth.UserGroup;
import pierro.lmsPlatform.Repository.Auth.RoleRepository;
import pierro.lmsPlatform.Repository.Auth.UserGroupRepository;
import pierro.lmsPlatform.Repository.Auth.UserRepository;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountService {
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final IdObfuscator idObfuscator;
    public void createUser(CreateUserDto createUser) {
        User user = new User();
        Role role = roleRepository.findByName(createUser.getRole())
                .orElseThrow(()->new RuntimeException("Role not found"));
        UserGroup  userGroup = userGroupRepository.findByName(createUser.getUserGroup())
                        .orElseThrow(()->new RuntimeException("Group not found"));
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(createUser.getPassword()));
        user.setUsername(createUser.getUsername());
        user.setUserGroup(userGroup);
        user.setEmail(createUser.getEmail());
        userRepository.save(user);
    }
    public void deleteUser(String Id) {
        Long userId = idObfuscator.decode(Id);
        userRepository.deleteById(userId);
    }
    public List<UserResposeDto> getAllUser(){
        List<User> users = userRepository.findAll();
        return listEntityToDto(users);
    }
    public List<UserResposeDto> getByRole(String name){
        Role role = roleRepository.findByName(name)
                .orElseThrow(()->new RuntimeException("Role not found"));
        return listEntityToDto(new ArrayList<>(role.getUsers()));
    }
    private List<UserResposeDto> listEntityToDto(List<User> users){
        List<UserResposeDto> userResposeDtos = new ArrayList<>();
        for(User user : users){
            UserResposeDto userResposeDto = new UserResposeDto();
            userResposeDto.setId(idObfuscator.encode(user.getId()));
            userResposeDto.setUsername(user.getUsername());
            userResposeDto.setEmail(user.getEmail());
            userResposeDto.setRole(user.getRole().getName());
            userResposeDtos.add(userResposeDto);
        }
        return userResposeDtos;
    }
    public void updateUser(UserResposeDto userResposeDto) {
        User user = userRepository.findById(idObfuscator.decode(userResposeDto.getId()))
                .orElseThrow(()->new RuntimeException("User not found"));
        user.setUsername(userResposeDto.getUsername());
        user.setEmail(userResposeDto.getEmail());
        user.setRole(roleRepository.findByName(userResposeDto.getUsername())
                .orElseThrow(()->new RuntimeException("Role not found")));
        userRepository.save(user);
    }
}
