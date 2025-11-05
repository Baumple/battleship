package org.shared;

public class Annotations {

    @Serializeable
    class Person {
        @Element(key = "name")
        private String name;

        private String address;

        @Init
        public void initNames() {
            this.name = this.name.substring(0, 1).toUpperCase()
                    + this.name.substring(1);
        }

        public Person(String name, String address) {
            this.name = name;
            this.address = address;
        }
    }

    private void checkIfSerializeable(Object o) {
        if (o == null)
            throw new RuntimeException();

        Class<?> clazz = o.getClass();
        if (!clazz.isAnnotationPresent(Serializeable.class)) {
            throw new RuntimeException(clazz.getSimpleName() + " not annotated");
        }
    }

    private void initializeObject(Object o) throws Exception {
        Class<?> clazz = o.getClass();
        for (var method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Init.class)) {
                method.setAccessible(true);
                method.invoke(o);
            }
        }
    }

    private String getJsonString(Object o) throws Exception {
        var clazz = o.getClass();
        java.util.Map<String, String> elements = new java.util.HashMap<>();

        for (var field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Element.class)) {
                elements.put(field.getName(), (String) field.get(o));
            }
        }
        String json = elements.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
                .collect(Collectors.joining(","));
        return "{" + json + "}";
    }

    public static void main(String[] args) {
    }

    public class ObjectJsonConverter {
        public String convertToJson(Object o) throws Exception {
            checkIfSerializeable(o);
            initializeObject(o);
            return getJsonString(o);
        }
    }

}
