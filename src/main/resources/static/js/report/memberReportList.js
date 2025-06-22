document.addEventListener("DOMContentLoaded", function(){
	let report_line = document.getElementsByClassName("report_line");
	for(let i = 0; i < report_line.length; i++){
		report_line[i].addEventListener("click", function(){
			let form = document.createElement("form");
			form.action = "/report/memberReportDetail";
			form.method = "post";
			
			let input = document.createElement("input");
			input.type = "hidden";
			input.name = "repId";
			input.value = this.getAttribute("data-repId");
			form.appendChild(input);
			
			document.body.appendChild(form);
			form.submit();
		});
	}
});