package ru.itmo.students.dto;

public class StudentRules {

    public static final String NAME = "[A-Za-zА-Яа-яЁё]+(?:[-'][A-Za-zА-Яа-яЁё]+)*(?: [A-Za-zА-Яа-яЁё]+(?:[-'][A-Za-zА-Яа-яЁё]+)*)+";
    public static final String GROUP = "[A-Z][34][1-4][0-9]{2}";
    public static final String ISU = "[1-9][0-9]{5}";
    public static final String ROOM = "(?:[1-9]|1[0-9]|20)(?:0[1-9]|[1-9][0-9])";

    public static final String REQUIRED = "Обязательное поле";
    public static final String NAME_MESSAGE = "Введите минимум фамилию и имя: только буквы, дефис и апостроф внутри слов";
    public static final String NAME_LENGTH_MESSAGE = "ФИО должно быть не длиннее 100 символов";
    public static final String GROUP_MESSAGE = "Группа: латинская заглавная буква, потом 3 или 4 и еще три цифры, например M3301";
    public static final String ISU_MESSAGE = "ИСУ должен состоять из 6 цифр, первая не 0";
    public static final String DORMITORY_MESSAGE = "Номер общежития от 1 до 8";
    public static final String ROOM_MESSAGE = "Комната: этаж от 1 до 20 и номер от 01 до 99, например 1203";
    public static final String NOTE_MESSAGE = "Заметки не длиннее 2000 символов";
}
