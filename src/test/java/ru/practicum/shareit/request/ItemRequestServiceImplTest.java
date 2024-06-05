package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.Storage.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.storage.UserRepository;
import ru.practicum.shareit.user.model.User;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static ru.practicum.shareit.util.Page.getSortedPage;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestMapper itemRequestMapper;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    private User user;
    private ItemRequestDto inputItemRequestDto;
    private ItemRequest savedItemRequest;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        user = new User(1, "User", "User@mail.ru");
        inputItemRequestDto = new ItemRequestDto(null, "Нужен лазерный нивелир",
                null);
        savedItemRequest = new ItemRequest(1, "Нужен лазерный нивелир", user, LocalDateTime.now());
        item = new Item(1, "Лазерный нивелир", "Лазерный нивелир EX600-Pro",
                true, 2);
        item.setRequest(savedItemRequest);
        itemDto = new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getIsAvailable(),
                item.getRequest().getId());
    }

    @Test
    void createItemRequest_whenUserFound_thenSaveItemRequest() {
        int userId = 1;
        ItemRequestDto expectedItemRequestDto = new ItemRequestDto(savedItemRequest.getId(),
                savedItemRequest.getDescription(), savedItemRequest.getCreationDate());

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestMapper.toItemRequest(user, inputItemRequestDto)).thenReturn(new ItemRequest());
        Mockito.when(itemRequestRepository.save(any())).thenReturn(savedItemRequest);
        Mockito.when(itemRequestMapper.toItemRequestDto(any())).thenReturn(expectedItemRequestDto);

        ItemRequestDto result = itemRequestService.createItemRequest(userId, inputItemRequestDto);

        assertEquals(expectedItemRequestDto, result);
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(itemRequestMapper).toItemRequest(user, inputItemRequestDto);
        Mockito.verify(itemRequestRepository).save(any());
        Mockito.verify(itemRequestMapper).toItemRequestDto(any());
    }

    @Test
    void createItemRequest_whenUserNotFound_thenThrowObjectNotFoundExceptionAndNotSaveItemRequest() {
        int userId = 0;

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.createItemRequest(userId, any()));
        Mockito.verify(itemRequestMapper, never()).toItemRequest(any(), any());
        Mockito.verify(itemRequestRepository, never()).save(any());
        Mockito.verify(itemRequestMapper, never()).toItemRequestDto(any());
    }

    @Test
    void getRequest_whenUserAndItemRequestFound_thenReturnedItemRequestDtoWithAnswers() {
        int userId = 1;
        int requestId = 1;
        ItemRequestDtoWithAnswers expectedDto = new ItemRequestDtoWithAnswers(
                1, "Нужен лазерный нивелир", LocalDateTime.now(), List.of(itemDto));

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(savedItemRequest));
        Mockito.when(itemRepository.findItemsByRequestId(requestId)).thenReturn(List.of(item));
        Mockito.when(itemRequestMapper.toItemRequestDtoWithAnswers(savedItemRequest, List.of(item)))
                .thenReturn(expectedDto);

        ItemRequestDtoWithAnswers result = itemRequestService.getRequest(userId, requestId);

        assertEquals(expectedDto, result);
        Mockito.verify(itemRequestRepository).findById(requestId);
        Mockito.verify(itemRepository).findItemsByRequestId(requestId);
        Mockito.verify(itemRequestMapper).toItemRequestDtoWithAnswers(savedItemRequest, List.of(item));
    }

    @Test
    void getRequest_UserNotFound_thenThrowObjectNotFoundException() {
        int userId = 0;
        int requestId = 1;

        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ObjectNotFoundException.class, () -> itemRequestService.getRequest(userId, requestId));
        Mockito.verify(itemRequestRepository, never()).findById(requestId);
        Mockito.verify(itemRepository, never()).findItemsByRequestId(requestId);
        Mockito.verify(itemRequestMapper, never()).toItemRequestDtoWithAnswers(any(), anyList());
    }

    @Test
    void getRequest_ItemRequestNotFound_thenThrowObjectNotFoundException() {
        int userId = 1;
        int requestId = 0;

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemRequestService.getRequest(userId, requestId));
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(itemRepository, never()).findItemsByRequestId(requestId);
        Mockito.verify(itemRequestMapper, never()).toItemRequestDtoWithAnswers(any(), anyList());
    }

    @Test
    void getRequestsWithItemsForRequester_whenUserAndItemsRequestsListAndAddedItemsListFound_thenInvoke4Methods() {
        int userId = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "creationDate");
        ItemRequestDtoWithAnswers expectedDto = new ItemRequestDtoWithAnswers(
                1, "Нужен лазерный нивелир", LocalDateTime.now(), List.of(itemDto));

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(itemRequestRepository.findItemsRequestsByRequesterId(userId, sort))
                .thenReturn(List.of(savedItemRequest));
        Mockito.when(itemRepository.findItemsByRequesterId(userId)).thenReturn(List.of(item));
        Mockito.when(itemRequestMapper.toItemRequestDtoWithAnswers(savedItemRequest, List.of(item)))
                .thenReturn(expectedDto);

        itemRequestService.getRequestsWithItemsForRequester(userId);

        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(itemRequestRepository).findItemsRequestsByRequesterId(userId, sort);
        Mockito.verify(itemRepository).findItemsByRequesterId(userId);
        Mockito.verify(itemRequestMapper, times(List.of(savedItemRequest).size()))
                .toItemRequestDtoWithAnswers(savedItemRequest, List.of(item));
    }

    @Test
    void getRequestsWithItemsForRequester_whenUserNotFound_thenThrowObjectNotFoundException() {
        int userId = 0;
        Sort sort = Sort.by(Sort.Direction.DESC, "creationDate");

        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ObjectNotFoundException.class, () -> itemRequestService.getRequestsWithItemsForRequester(userId));
        Mockito.verify(itemRequestRepository, never()).findItemsRequestsByRequesterId(userId, sort);
        Mockito.verify(itemRepository, never()).findItemsByRequesterId(userId);
        Mockito.verify(itemRequestMapper, never()).toItemRequestDtoWithAnswers(any(), anyList());
    }

    @Test
    void getRequestsWithItemsForRequester_whenItemsRequestsListNotFound_thenReturnedEmptyItemsRequestsList() {
        int userId = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "creationDate");

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(itemRequestRepository.findItemsRequestsByRequesterId(userId, sort)).thenReturn(anyList());

        List<ItemRequestDtoWithAnswers> result = itemRequestService.getRequestsWithItemsForRequester(userId);

        assertTrue(result.isEmpty());
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(itemRequestRepository).findItemsRequestsByRequesterId(userId, sort);
        Mockito.verify(itemRepository, never()).findItemsByRequesterId(userId);
        Mockito.verify(itemRequestMapper, never()).toItemRequestDtoWithAnswers(any(ItemRequest.class), anyList());
    }

    @Test
    void getOtherRequestsWithItems_whenUserExistAndItemsRequestsListAndAddedItemsListFound_thenInvoke4Methods() {
        int userId = 3;
        int page = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "creationDate");
        Pageable pageable = getSortedPage(page, size, sort);
        List<Integer> requestsIdsList = List.of(savedItemRequest.getId());
        ItemRequestDtoWithAnswers expectedDto = new ItemRequestDtoWithAnswers(
                1, "Нужен лазерный нивелир", LocalDateTime.now(), List.of(itemDto));

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(itemRequestRepository.findItemsRequestsByRequesterIdNot(userId, pageable))
                .thenReturn(List.of(savedItemRequest));
        Mockito.when(itemRepository.findItemsByRequestIdIn(requestsIdsList)).thenReturn(List.of(item));
        Mockito.when(itemRequestMapper.toItemRequestDtoWithAnswers(savedItemRequest, List.of(item)))
                .thenReturn(expectedDto);

        List<ItemRequestDtoWithAnswers> result = itemRequestService.getOtherRequestsWithItems(userId, page, size);

        assertTrue(result.size() <= size);
        Mockito.verify(userRepository).existsById(userId);
        Mockito.verify(itemRequestRepository).findItemsRequestsByRequesterIdNot(userId, pageable);
        Mockito.verify(itemRepository).findItemsByRequestIdIn(requestsIdsList);
        Mockito.verify(itemRequestMapper, times(List.of(savedItemRequest).size()))
                .toItemRequestDtoWithAnswers(savedItemRequest, List.of(item));
    }

    @Test
    void getOtherRequestsWithItems_whenUserNotFound_thenThrowObjectNotFoundException() {
        int userId = 1;
        int page = 0;
        int size = 10;
        Sort sort = Sort.by(Sort.Direction.DESC, "creationDate");
        Pageable pageable = getSortedPage(page, size, sort);

        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ObjectNotFoundException.class,
                () -> itemRequestService.getOtherRequestsWithItems(userId, page, size));
        Mockito.verify(itemRequestRepository, never()).findItemsRequestsByRequesterIdNot(userId, pageable);
        Mockito.verify(itemRepository, never()).findItemsByRequestIdIn(anyList());
        Mockito.verify(itemRequestMapper, never()).toItemRequestDtoWithAnswers(any(ItemRequest.class), anyList());
    }
}