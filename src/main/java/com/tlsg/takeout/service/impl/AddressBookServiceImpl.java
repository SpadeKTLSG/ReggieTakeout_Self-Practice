package com.tlsg.takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tlsg.takeout.entity.AddressBook;
import com.tlsg.takeout.mapper.AddressBookMapper;
import com.tlsg.takeout.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
