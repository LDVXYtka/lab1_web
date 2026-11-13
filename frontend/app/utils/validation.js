export function validateStudent(student) {
  const errors = {}
  const required = ['name', 'group', 'isu', 'dormitory', 'room', 'term']
  for (const field of required) {
    if (student[field] === '' || student[field] === null) {
      errors[field] = 'Обязательное поле'
    }
  }
  if (student.name && !/^[A-Za-zА-Яа-яЁё]+(?:[-'][A-Za-zА-Яа-яЁё]+)*(?: [A-Za-zА-Яа-яЁё]+(?:[-'][A-Za-zА-Яа-яЁё]+)*)+$/.test(student.name)) {
    errors.name = 'Введите минимум фамилию и имя, используя буквы, дефис или апостроф внутри слов'
  }
  if (student.name.length > 100) {
    errors.name = 'ФИО должно быть не длиннее 100 символов'
  }
  if (student.group && !/^[A-Z][34][1-4][0-9]{2}$/.test(student.group)) {
    errors.group = 'Группа: латинская заглавная буква, 3 или 4 и еще три цифры, например M3301'
  }
  if (student.isu && !/^[1-9][0-9]{5}$/.test(student.isu)) {
    errors.isu = 'ИСУ должен состоять из 6 цифр, первая не 0'
  }
  if (student.dormitory !== null && (!Number.isInteger(student.dormitory) || student.dormitory < 1 || student.dormitory > 8)) {
    errors.dormitory = 'Номер общежития от 1 до 8'
  }
  if (student.room && !/^(?:[1-9]|1[0-9]|20)(?:0[1-9]|[1-9][0-9])$/.test(student.room)) {
    errors.room = 'Комната: этаж от 1 до 20 и номер от 01 до 99, например 1203'
  }
  const maxTerm = new Date().getFullYear() + '-12-31'
  if (student.term && (student.term < '1970-01-01' || student.term > maxTerm || Number.isNaN(Date.parse(student.term)))) {
    errors.term = 'Дата заселения от 1970-01-01 до ' + maxTerm
  }
  if (student.note.length > 2000) {
    errors.note = 'Заметки не длиннее 2000 символов'
  }
  return errors
}

export function validateLogin(user) {
  const errors = {}
  if (!user.username.trim()) errors.username = 'Введите имя пользователя'
  else if (!/^[A-Za-z0-9_]{3,32}$/.test(user.username.trim())) {
    errors.username = 'Имя пользователя: от 3 до 32 латинских букв, цифр или знаков _'
  }
  if (!user.password) errors.password = 'Введите пароль'
  else if (!/^[!-~]{8,72}$/.test(user.password)) {
    errors.password = 'Пароль: от 8 до 72 латинских букв, цифр или знаков, без пробелов'
  }
  return errors
}

export function validateRegister(user) {
  const errors = validateLogin(user)
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(user.email.trim()) || user.email.trim().length > 254) {
    errors.email = 'Введите почту в формате name@example.com, не длиннее 254 символов'
  }
  return errors
}
