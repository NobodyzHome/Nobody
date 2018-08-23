create table base_line(
	line_no integer primary key identity,
	line_code varchar(20),
	line_name varchar(20),
	modify_time datetime,
	is_run integer,
	dept_no integer
);