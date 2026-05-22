package br.com.clinica.agendamento.client.dto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "admin-service", path = "/api/convenios")
public interface AdminClient {

    @GetMapping("/{id}")
    ConvenioResponse buscarConvenioPorId(@PathVariable("id") Long id);
}