package ru.bsuedu.cad.lab;

import org.springframework.stereotype.Component;

@Component("HTML")
public class HTMLTableRenderer implements Renderer {

    private Provider<Product> provider;

    public HTMLTableRenderer(Provider<Product> productProvider){
        provider = productProvider;
    }

    @Override
    public void render() {
        provider.getEntitees();
        System.out.println("Вывод в HTML файл");
    }
    
}
