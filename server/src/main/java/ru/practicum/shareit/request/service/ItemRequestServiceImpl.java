package ru.practicum.shareit.request.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.util.Page.getSortedPage;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestMapper itemRequestMapper;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestMapper itemRequestMapperArg,
                                  ItemRequestRepository itemRequestRepositoryArg,
                                  UserRepository userRepositoryArg, ItemRepository itemRepositoryArg) {
        itemRequestMapper = itemRequestMapperArg;
        itemRequestRepository = itemRequestRepositoryArg;
        userRepository = userRepositoryArg;
        itemRepository = itemRepositoryArg;
    }

    @Override
    public ItemRequestDto createItemRequest(int userId, ItemRequestDto itemRequestDto) {
        Optional<User> optionalRequester = userRepository.findById(userId);
        if (optionalRequester.isEmpty()) {
            String message = String.format("Пользователь c id=%d не найден. Создание запроса не возможно!", userId);
            throw new ObjectNotFoundException(message);
        }
        User requester = optionalRequester.get();
        ItemRequest newItemRequest = itemRequestMapper.toItemRequest(requester, itemRequestDto);
        ItemRequest createdItemRequest = itemRequestRepository.save(newItemRequest);
        return itemRequestMapper.toItemRequestDto(createdItemRequest);
    }

    @Override
    public List<ItemRequestDtoWithAnswers> getRequestsWithItemsForRequester(int userId) {
        checkUserExistence(userId);

        List<ItemRequestDtoWithAnswers> result = new ArrayList<>();
        // находим все запросы у определенного пользователя, отсортированные по дате создания
        Sort sortedByCreationDate = Sort.by(Sort.Direction.DESC, "creationDate");
        List<ItemRequest> requests = itemRequestRepository.findItemsRequestsByRequesterId(userId, sortedByCreationDate);
        if (requests.isEmpty()) {
            return result;
        }
        // находим все добавленные вещи на запрос определенного пользователя
        List<Item> items = itemRepository.findItemsByRequesterId(userId);

        for (ItemRequest request : requests) {
            int id = request.getId();
            List<Item> answers = findItemsForCurrentRequest(id, items);
            ItemRequestDtoWithAnswers dtoWithAnswers = itemRequestMapper.toItemRequestDtoWithAnswers(request, answers);
            result.add(dtoWithAnswers);
        }
        return result;
    }

    @Override
    public List<ItemRequestDtoWithAnswers> getOtherRequestsWithItems(int userId, int page, int size) {
        checkUserExistence(userId);

        List<ItemRequestDtoWithAnswers> result = new ArrayList<>();
        Sort sortedByCreationDate = Sort.by(Sort.Direction.DESC, "creationDate");
        // находим список запросов, созданных другими пользователями, размера size
        List<ItemRequest> requests = itemRequestRepository
                .findItemsRequestsByRequesterIdNot(userId, getSortedPage(page, size, sortedByCreationDate));
        if (requests.isEmpty()) {
            return result;
        }
        // находим вещи, которые добавили другие пользователи на запрос
        List<Integer> requestIds = getRequestIds(requests);
        List<Item> items = itemRepository.findItemsByRequestIdIn(requestIds);

        for (ItemRequest request : requests) {
            int id = request.getId();
            List<Item> answers = findItemsForCurrentRequest(id, items);
            ItemRequestDtoWithAnswers dtoWithAnswers = itemRequestMapper.toItemRequestDtoWithAnswers(request, answers);
            result.add(dtoWithAnswers);
        }
        return result;
    }

    @Override
    public ItemRequestDtoWithAnswers getRequest(int userId, int requestId) {
        checkUserExistence(userId);
        Optional<ItemRequest> optionalItemRequest = itemRequestRepository.findById(requestId);
        if (optionalItemRequest.isEmpty()) {
            String message = String.format("Запрос с id=%d на добавление новой вещи не найден!", requestId);
            throw new ObjectNotFoundException(message);
        }
        ItemRequest itemRequest = optionalItemRequest.get();
        int id = itemRequest.getId();
        List<Item> items = itemRepository.findItemsByRequestId(id);
        return itemRequestMapper.toItemRequestDtoWithAnswers(itemRequest, items);
    }

    private void checkUserExistence(int userId) {
        boolean isUserExist = userRepository.existsById(userId);
        if (!isUserExist) {
            String message = String.format("Пользователь c id=%d не найден", userId);
            throw new ObjectNotFoundException(message);
        }
    }

    private List<Item> findItemsForCurrentRequest(int requestId, List<Item> items) {
        List<Item> result = new ArrayList<>();
        if (items.isEmpty()) {
            return result;
        }
        for (Item item : items) {
            if (item.getRequest().getId() == requestId) {
                result.add(item);
            }
        }
        return result;
    }

    private List<Integer> getRequestIds(List<ItemRequest> requests) {
        List<Integer> result = new ArrayList<>();
        for (ItemRequest request : requests) {
            Integer id = request.getId();
            result.add(id);
        }
        return result;
    }
}
