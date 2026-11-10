create function notify_student_changes() returns trigger
language plpgsql as $$
begin
    if TG_OP = 'DELETE' then
        perform pg_notify('student_changes', OLD.id::text);
    else
        perform pg_notify('student_changes', NEW.id::text);
    end if;
    return null;
end;
$$;

create trigger students_changed
after insert or update or delete on students
for each row execute function notify_student_changes();
