package com.flooring.flooringmastery.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;


@Component
public class UserIOConsoleImpl implements UserIO {

    private final Scanner console = new Scanner(System.in);

    public void print(String message){
        System.out.println(message);
    }

    public String readString(String prompt){
        System.out.print(prompt);
        String input = console.nextLine();
        while(input == null){
            System.out.print("please Enter a valid entry");
            input = console.nextLine();
        }
        return input;
    }

    public int readInt(String prompt){
        System.out.print(prompt);
        while(true){
            try{
                return Integer.parseInt(console.nextLine());
            }
            catch(NumberFormatException e){
                System.out.println("Please enter a Valid integer");
            }
        }

    }

    public int readInt(String prompt, int min, int max){
        System.out.print(prompt);
        while(true){
            try{
                int x= Integer.parseInt(console.nextLine());
                if(x<min || x>max){
                    System.out.println("Please enter a Valid integer");
                    continue;
                }else{
                    return x;
                }
            }catch(NumberFormatException e){
                System.out.println("Please enter a Valid integer between "+min+" and "+max);
            }
        }
    }

    public boolean readYesNo(String prompt){
        List<String> yesNo = List.of("y", "yes", "n", "no");
        String answer;
        System.out.print(prompt);
        while(true){
            answer = console.nextLine().toLowerCase();
            if(yesNo.subList(0,2).contains(answer)){
                return true;
            } else if (yesNo.subList(2,4).contains(answer)) {
                return false;
            }else{
                System.out.println("Please enter a valid option (y/n)");
            }
        }

    }

    public BigDecimal readBigDecimal(String prompt){
        System.out.print(prompt);
        while(true){
            try{
                return new BigDecimal(console.nextLine());
            }
            catch(NumberFormatException e){
                System.out.println("Please enter a Valid integer");
            }
        }
    }

    public BigDecimal readBigDecimal(String prompt, BigDecimal min){
        System.out.print(prompt);
        while(true){
            try {
                BigDecimal x=new BigDecimal(console.nextLine());
                if (x.compareTo(min)!=1 ) {
                    System.out.println("Please enter a value bigger than "+min);
                    continue;
                } else {
                    return x;
                }
            }catch(NumberFormatException e){
                System.out.println("Please enter a Valid number");
            }
        }
    }

    public LocalDate readDate(String prompt){
        System.out.print(prompt);
        while(true){
            try{
                return LocalDate.parse(console.nextLine());
            }catch (Exception e){
                System.out.println("Please enter a valid date Format (YYYY-MM-DD)");
            }

        }
    }

    //used for edit so empty string will be allowed
    public String readStringAllowEmpty(String prompt){
        System.out.print(prompt);
        return console.nextLine();
    }

    // File helper implementations
    @Override
    public List<String> readAllLines(String path) throws IOException {
        Path p = Paths.get(path);
        if (!Files.exists(p)) return new ArrayList<>();
        return Files.readAllLines(p);
    }

    @Override
    public void writeLines(String path, List<String> lines) throws IOException {
        Path p = Paths.get(path);
        if (p.getParent() != null) Files.createDirectories(p.getParent());
        Files.write(p, lines);
    }

    @Override
    public boolean exists(String path) throws IOException {
        return Files.exists(Paths.get(path));
    }

    @Override
    public void deleteIfExists(String path) throws IOException {
        Files.deleteIfExists(Paths.get(path));
    }

    @Override
    public void createDirectories(String dirPath) throws IOException {
        Path p = Paths.get(dirPath);
        if (!Files.exists(p)) Files.createDirectories(p);
    }
}
