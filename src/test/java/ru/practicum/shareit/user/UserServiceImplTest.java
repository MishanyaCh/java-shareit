package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.EmailValidationException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;
    private UserDto inputUserDto;
    private User user;
    private User savedUser;
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @BeforeEach
    void setUp() {
        inputUserDto = new UserDto(null, "User", "User@mail.ru");
        user = new User(0, "User", "User@mail.ru");
        savedUser = new User(1, "User", "User@mail.ru");
    }

    @Test
    void createUser_whenInvoke_thenSaveUser() {
        UserDto expectedUserDto = new UserDto(1, "User", "User@mail.ru");

        Mockito.when(userMapper.toUser(inputUserDto)).thenReturn(user);
        Mockito.when(userRepository.save(any())).thenReturn(savedUser);
        Mockito.when(userMapper.toUserDto(any())).thenReturn(expectedUserDto);

        UserDto result = userService.createUser(inputUserDto);

        assertEquals(expectedUserDto, result);
        Mockito.verify(userMapper).toUser(inputUserDto);
        Mockito.verify(userRepository).save(any());
        Mockito.verify(userMapper).toUserDto(any());
    }

    @Test
    void createUser_whenHasUserWithDuplicateEmail_thenThrowEmailValidationExceptionAndNotSaveUser() {
        Mockito.when(userMapper.toUser(inputUserDto)).thenReturn(user);
        Mockito.when(userRepository.save(any())).thenThrow(new RuntimeException());

        assertThrows(EmailValidationException.class, () -> userService.createUser(inputUserDto));
        Mockito.verify(userMapper).toUser(inputUserDto);
        Mockito.verify(userRepository).save(any());
        Mockito.verify(userMapper, never()).toUserDto(any());
    }

    @Test
    void updateUser_whenUserFound_thenUpdateUser() {
        int userId = 1;
        inputUserDto = new UserDto(null, "UpdatedUser", "User@gmail.com");
        User updatedUser = new User(1, "UpdatedUser", "User@gmail.com");
        UserDto expectedUserDto = new UserDto(1, "UpdatedUser", "User@gmail.com");
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        Mockito.when(userMapper.toUser(inputUserDto))
                .thenReturn(new User(0, "UpdatedUser", "User@gmail.com"));
        Mockito.when(userRepository.save(any())).thenReturn(updatedUser);
        Mockito.when(userMapper.toUserDto(updatedUser)).thenReturn(expectedUserDto);

        UserDto result = userService.updateUser(userId, inputUserDto);

        assertEquals(expectedUserDto, result);
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userMapper).toUser(inputUserDto);
        Mockito.verify(userRepository).save(userArgumentCaptor.capture());
        User updatedUserInDb = userArgumentCaptor.getValue();
        assertEquals("UpdatedUser", updatedUserInDb.getName());
        assertEquals("User@gmail.com", updatedUserInDb.getEmail());
        Mockito.verify(userMapper).toUserDto(updatedUser);
    }

    @Test
    void updateUser_whenUserNotFound_thenThrowObjectNotFoundExceptionAndNotUpdateUser() {
        int userId = 0;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> userService.updateUser(userId, inputUserDto));
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userMapper, never()).toUser(any());
        Mockito.verify(userRepository, never()).save(any());
        Mockito.verify(userMapper, never()).toUserDto(any());
    }

    @Test
    void getUsers_whenInvoke_thenReturnedUserList() {
        UserDto userDto = new UserDto(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
        List<UserDto> expectedDto = List.of(userDto);

        Mockito.when(userRepository.findAll()).thenReturn(List.of(savedUser));
        Mockito.when(userMapper.toUserDtoList(any())).thenReturn(expectedDto);

        List<UserDto> result = userService.getUsers();

        assertEquals(expectedDto.get(0), result.get(0));
        Mockito.verify(userRepository).findAll();
        Mockito.verify(userMapper).toUserDtoList(List.of(savedUser));
    }

    @Test
    void getUsers_whenHaveNotUsers_thenReturnEmptyList() {
        Mockito.when(userRepository.findAll()).thenReturn(new ArrayList<>());
        Mockito.when(userMapper.toUserDtoList(any())).thenReturn(new ArrayList<>());

        List<UserDto> result = userService.getUsers();

        assertTrue(result.isEmpty());
        Mockito.verify(userRepository).findAll();
        Mockito.verify(userMapper).toUserDtoList(any());
    }

    @Test
    void getUser_whenUserFound_thenReturnedUserDto() {
        int userId = 1;
        UserDto expectedUserDto = new UserDto(1, "User", "User@mail.ru");

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        Mockito.when(userMapper.toUserDto(any())).thenReturn(expectedUserDto);

        UserDto result = userService.getUser(userId);

        assertEquals(expectedUserDto, result);
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userMapper).toUserDto(any());
    }

    @Test
    void getUser_whenUserNotFound_thenThrowObjectNotFoundException() {
        int userId = 0;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> userService.getUser(userId));
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userMapper, never()).toUserDto(any());
    }

    @Test
    void deleteUser_whenUserFound_thenDeleteUser() {
        int userId = 1;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        Mockito.doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_whenUserNotFound_thenThrowObjectNotFoundException() {
        int userId = 0;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> userService.deleteUser(userId));
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userRepository, never()).deleteById(userId);
    }
}