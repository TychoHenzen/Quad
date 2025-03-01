package com.quadexercise.quad.controller;

import com.quadexercise.quad.interfaces.ITriviaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SuppressWarnings("DuplicateStringLiteralInspection")
@Controller
public class ViewController {

    private final ITriviaService _triviaService;

    public ViewController(ITriviaService triviaService) {
        _triviaService = triviaService;
    }

    @GetMapping("/")
    public String home() {
        return "homeTemplate";
    }

    @GetMapping("/play")
    public String playTrivia(@RequestParam(defaultValue = "5") int amount, Model model) {
        model.addAttribute("questions", _triviaService.getQuestions(amount));
        return "triviaTemplate";
    }

    @GetMapping("/results")
    public String results() {
        return "resultsTemplate";
    }
}
