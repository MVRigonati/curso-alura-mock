package br.com.caelum.leilao.dominio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.email.EnviadorDeEmail;
import br.com.caelum.leilao.servico.EncerradorDeLeilao;

public class EncerradorDeLeilaoTest {
	
	private Leilao leilaoCarroSemanaPassada;
	private Leilao leilaoGeladeiraSemanaPassada;
	private Leilao leilaoCarroOntem;
	private Leilao leilaoGeladeiraOntem;
	
	private EncerradorDeLeilao encerrador;
	
	@Mock
	private RepositorioDeLeiloes daoMock;
	@Mock
	private EnviadorDeEmail emailMock;
	@Mock
	private Logger log;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);

		final Calendar semanaPassada = Calendar.getInstance();
		semanaPassada.add(Calendar.DAY_OF_MONTH, -7);
		final Calendar ontem = Calendar.getInstance();
		ontem.add(Calendar.DAY_OF_MONTH, -1);
		
		final CriadorDeLeilao criadorDeLeilao = new CriadorDeLeilao();
		leilaoCarroSemanaPassada = criadorDeLeilao
				.para("L1").naData(semanaPassada).constroi();
		leilaoGeladeiraSemanaPassada = criadorDeLeilao
				.para("L2").naData(semanaPassada).constroi();
		leilaoCarroOntem = criadorDeLeilao
				.para("L3").naData(ontem).constroi();
		leilaoGeladeiraOntem = criadorDeLeilao
				.para("L4").naData(ontem).constroi();
		
		doNothing().when(log).severe(anyString());
		doNothing().when(log).warning(anyString());
		
		encerrador = new EncerradorDeLeilao(daoMock, emailMock, log);
	}
	
	@Test
	public void deveEncerrarLeiloesCriadosAUmaSemana() {
		when(daoMock.correntes()).thenReturn(Arrays.asList(leilaoCarroSemanaPassada, leilaoGeladeiraSemanaPassada));
		encerrador.encerra();
		
		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilaoCarroSemanaPassada.isEncerrado());
		assertTrue(leilaoGeladeiraSemanaPassada.isEncerrado());
	}
	
	@Test
	public void naoDeveEncerrarLeilaoCriadoAMenosDeUmaSemana() {
		when(daoMock.correntes()).thenReturn(Arrays.asList(leilaoCarroOntem, leilaoGeladeiraOntem));
		encerrador.encerra();
		
		assertEquals(0, encerrador.getTotalEncerrados());
		assertFalse(leilaoCarroOntem.isEncerrado());
		assertFalse(leilaoGeladeiraOntem.isEncerrado());
	}

	@Test
	public void naoDeveRealizarAcoesQuandoNaoExistirLeiloesCorrentes() {
		when(daoMock.correntes()).thenReturn(new ArrayList<Leilao>());
		encerrador.encerra();
		
		assertEquals(0, encerrador.getTotalEncerrados());
		verify(daoMock, never()).atualiza(any(Leilao.class));
		verify(emailMock, never()).envia(any(Leilao.class));
	}
	
	@Test
	public void deveAtualizarBancoEEnviarEmailAoEncerrarLeilao() {
		when(daoMock.correntes()).thenReturn(Arrays.asList(leilaoCarroSemanaPassada));
		encerrador.encerra();
		
		InOrder inOrder = Mockito.inOrder(daoMock, emailMock);
		
		assertEquals(1, encerrador.getTotalEncerrados());
		assertTrue(leilaoCarroSemanaPassada.isEncerrado());
		inOrder.verify(daoMock, times(1)).atualiza(any(Leilao.class));
		inOrder.verify(emailMock, times(1)).envia(any(Leilao.class));
	}
	
	@Test
    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {
        when(daoMock.correntes()).thenReturn(Arrays.asList(leilaoCarroOntem, leilaoGeladeiraOntem));
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilaoCarroOntem.isEncerrado());
        assertFalse(leilaoGeladeiraOntem.isEncerrado());
        verify(daoMock, never()).atualiza(any(Leilao.class));
        verify(emailMock, never()).envia(any(Leilao.class));
    }
	
	@Test
    public void emailNaoDeveSerEnviadoQuandoDaoJogarExcecao() {
		when(daoMock.correntes()).thenReturn(Arrays.asList(leilaoCarroSemanaPassada));
		doThrow(new RuntimeException()).when(daoMock).atualiza(any(Leilao.class));
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilaoCarroSemanaPassada.isEncerrado());
        verify(daoMock, times(1)).atualiza(any(Leilao.class));
        verify(emailMock, never()).envia(any(Leilao.class));
	}
	
	@Test
    public void deveContinuarExecucaoMesmoQuandoDAOJogaExcecao() {
		when(daoMock.correntes()).thenReturn(Arrays.asList(leilaoCarroSemanaPassada, leilaoGeladeiraSemanaPassada));
		doThrow(new RuntimeException()).when(daoMock).atualiza(leilaoCarroSemanaPassada);
        encerrador.encerra();

        assertEquals(1, encerrador.getTotalEncerrados());
        assertFalse(leilaoCarroSemanaPassada.isEncerrado());
        assertTrue(leilaoGeladeiraSemanaPassada.isEncerrado());
        verify(daoMock, times(2)).atualiza(any(Leilao.class));
        verify(emailMock, times(1)).envia(any(Leilao.class));
	}
	
	@Test
    public void deveContinuarExecucaoMesmoQuandoEmailJogaExcecao() {
		when(daoMock.correntes()).thenReturn(Arrays.asList(leilaoCarroSemanaPassada, leilaoGeladeiraSemanaPassada));
		doThrow(new NullPointerException()).when(emailMock).envia(leilaoCarroSemanaPassada);
        encerrador.encerra();

        assertEquals(2, encerrador.getTotalEncerrados());
        assertTrue(leilaoCarroSemanaPassada.isEncerrado());
        assertTrue(leilaoGeladeiraSemanaPassada.isEncerrado());
        verify(daoMock, times(2)).atualiza(any(Leilao.class));
        verify(emailMock, times(2)).envia(any(Leilao.class));
	}
	
}
