package com.salgados.RSalgados.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller principal da aplicação.
 * 
 * <p>Gerencia a página inicial e outras rotas gerais do sistema.
 * 
 * <p>Este controller não possui um {@code @RequestMapping} base,
 * respondendo diretamente às rotas raiz da aplicação.
 * 
 * @author RSalgados Team
 * @version 1.0
 * @since 2026-01-31
 */
@Controller
public class MainController {
    
    /**
     * Exibe a página inicial da aplicação.
     * 
     * <p>Rota: {@code GET /}
     * 
     * @param model modelo Spring MVC para passar dados à view
     * @return nome da view {@code home/index}
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("titulo", "Início");
        return "home/index";
    }
}