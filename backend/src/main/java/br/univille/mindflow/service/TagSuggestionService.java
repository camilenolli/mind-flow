package br.univille.mindflow.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extração de tags por frequência de termos — 100% local, sem custo, sem API externa.
 * Prioriza siglas, termos capitalizados e palavras com alta frequência após remover stopwords PT-BR.
 */
@Service
public class TagSuggestionService {

    private static final Set<String> STOPWORDS = Set.of(
        "a","ao","aos","as","até","com","como","da","das","de","dela","delas","dele","deles",
        "dem","depois","do","dos","e","é","ela","elas","ele","eles","em","entre","era","essa",
        "essas","esse","esses","esta","estas","este","estes","eu","foi","for","foram","havia",
        "isso","isto","já","lhe","lhes","mais","mas","me","mesmo","meu","minha","muito","na",
        "nas","nem","no","nos","não","nós","num","numa","o","onde","os","ou","para","pela",
        "pelas","pelo","pelos","por","porque","que","quando","quem","se","seja","sem","ser",
        "seu","seus","si","sim","sobre","sua","suas","também","te","tem","tendo","ter","toda",
        "todas","todo","todos","tu","tua","tuas","um","uma","umas","uns","você","vós","à",
        "às","àquela","àquele","àqueles","àquelas","há","lá","só","nele","nela","neles","nelas",
        "aqui","ali","então","assim","qual","quais","pois","pode","podem","deve","devem",
        "são","foi","ser","está","são","foi","ser","esse","esta","quando","onde","como"
    );

    public List<String> suggest(String title, String content) {
        String combined = (title == null ? "" : title) + " " + (content == null ? "" : content);

        // 1. Extrair siglas e termos totalmente maiúsculos (ex: DNS, HTTP, SECI)
        List<String> acronyms = Arrays.stream(combined.split("[\\s\\p{Punct}]+"))
            .filter(w -> w.length() >= 2 && w.equals(w.toUpperCase()) && w.matches("[A-Z0-9]+"))
            .distinct()
            .limit(3)
            .collect(Collectors.toList());

        // 2. Tokenizar, normalizar e filtrar stopwords
        Map<String, Long> freq = Arrays.stream(combined.toLowerCase().split("[\\s\\p{Punct}\\d]+"))
            .filter(w -> w.length() >= 4)
            .filter(w -> !STOPWORDS.contains(w))
            .filter(w -> w.matches("[a-záàâãéêíóôõúüç]+"))
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        // 3. Top palavras por frequência
        List<String> topWords = freq.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .limit(6)
            .collect(Collectors.toList());

        // 4. Combinar siglas + palavras, remover duplicatas, limitar a 7
        List<String> result = Stream.concat(acronyms.stream(), topWords.stream())
            .distinct()
            .limit(7)
            .collect(Collectors.toList());

        // Capitalizar primeira letra de cada tag
        return result.stream()
            .map(t -> Character.toUpperCase(t.charAt(0)) + t.substring(1))
            .collect(Collectors.toList());
    }
}
