package com.example.servingwebcontent.Service;

import com.example.servingwebcontent.Repository.IPAddressRepository;
import com.example.servingwebcontent.model.IPAddress;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IPAddressService {

	private final IPAddressRepository ipAddressRepository;

	public IPAddressService(IPAddressRepository ipAddressRepository) {
		this.ipAddressRepository = ipAddressRepository;
	}

	public IPAddress getOrCreate(String value) {
		String id = "IPAddress:" + value;
		return ipAddressRepository.findById(id).orElseGet(() -> ipAddressRepository.save(new IPAddress(value)));
	}

	public Optional<IPAddress> findByValue(String value) {
		return ipAddressRepository.findByValue(value);
	}

	public List<IPAddress> findAll() {
		return ipAddressRepository.findAll();
	}
}
