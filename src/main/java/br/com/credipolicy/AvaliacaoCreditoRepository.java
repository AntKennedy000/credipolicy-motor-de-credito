package br.com.credipolicy;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoCreditoRepository
        extends JpaRepository<AvaliacaoCreditoEntity, UUID> {
}