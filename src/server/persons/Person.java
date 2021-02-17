package server.persons;

public class Person {
    private String name;
    private String type;
    private String division;

    public Person(String input){
        String[] fields = input.split(",");
        name = fields[0].substring(3);
        type = fields[1].substring(4);
        division = fields[2].substring(3);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDivision() {
        return division;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", division='" + division + '\'' +
                '}';
    }
}
