package br.com.awesome.filters;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * Servlet Filter implementation class JPAFilter
 */
@WebFilter("/JPAFilter")
public class JPAFilter implements Filter {
	
	private EntityManagerFactory entityManagerFactory;
	private String persistence_unit_name = "unit_app";

    /**
     * Default constructor. 
     */
    public JPAFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		this.entityManagerFactory.close();
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		/*CRIANDO UM ENTITYMANAGER*/
		EntityManager entityManager =  this.entityManagerFactory.createEntityManager();
 
		/*ADICIONANDO ELE NA REQUISIÇÃO*/
		request.setAttribute("entityManager", entityManager);
 
		/*INICIANDO UMA TRANSAÇÃO*/
		entityManager.getTransaction().begin();
 
		/*INICIANDO FACES SERVLET*/
		chain.doFilter(request, response);
 
		try {
			/*SE NÃO TIVER ERRO NA OPERAÇÃO ELE EXECUTA O COMMIT*/
			entityManager.getTransaction().commit();
 
		} catch (Exception e) {
			/*SE TIVER ERRO NA OPERAÇÃO É EXECUTADO O rollback*/
			entityManager.getTransaction().rollback();
		}
		finally{
			/*DEPOIS DE DAR O COMMIT OU ROLLBACK ELE FINALIZA O entityManager*/
			entityManager.close();
		}
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		/*CRIA O entityManagerFactory COM OS PARÂMETROS DEFINIDOS NO persistence.xml*/
		this.entityManagerFactory = Persistence.createEntityManagerFactory(this.persistence_unit_name); 
	}

}
