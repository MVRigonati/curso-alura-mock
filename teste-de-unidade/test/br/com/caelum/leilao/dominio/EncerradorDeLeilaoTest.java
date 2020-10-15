package br.com.caelum.leilao.dominio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.servico.EncerradorDeLeilao;

public class EncerradorDeLeilaoTest {
	
	@Test
	public void deveEncerrarLeilaoCriadoAMaisDeUmaSemana() {
		final Calendar semanaPassada = Calendar.getInstance();
		semanaPassada.add(Calendar.DAY_OF_MONTH, -7);
		
		final Leilao leilao1 = new CriadorDeLeilao().para("Carro").naData(semanaPassada).constroi();
		final Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(semanaPassada).constroi();
		
		final LeilaoDao daoMock = mock(LeilaoDao.class);
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		when(daoMock.correntes()).thenReturn(leiloesAntigos);
		
		final EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoMock);
		encerrador.encerra();
		
		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
	}

}
