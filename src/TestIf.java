import org.antlr.v4.runtime.TokenStreamRewriter;

public class TestIf extends JavaParserBaseListener{

    int count;
    TokenStreamRewriter rewriter;

    public TestIf(TokenStreamRewriter rewriter) {
        this.rewriter = rewriter;
        this.count=0;
    }

    @Override
    public void enterStatement(JavaParser.StatementContext ctx) {
        String code="";
        String span_id="expr_"+count;
        if ( ctx.FOR() !=null ){
            if(ctx.statement(0).block()== null)
            {
                //count++;
                rewriter.insertAfter(ctx.RPAREN().getSymbol(),"{");
            }
             String for_control  = ctx.forControl().expression().getText().trim();

             code=branch_coverage_check(for_control);

             if (code != "")
            {
                rewriter_html.insertBefore(ctx.forControl().getStart(), "<span  class=\"global_span\" id=\""+span_id+"\">");
                rewriter_html.insertAfter(ctx.forControl().getStop(), "</span>");
            }


        }
        if ( ctx.WHILE() !=null  || ctx.IF()!=null){
            if(ctx.statement(0).block()== null)
            {
                rewriter.insertAfter(ctx.parExpression().RPAREN().getSymbol(),"{" );//#@#
            }
            String s  = ctx.parExpression().expression().getText().trim();
            code=branch_coverage_check(s);
            if (code != "")
            {
                rewriter_html.insertBefore(ctx.parExpression().getStart(), "<span  class=\"global_span\" id=\"" + span_id + "\">");
                rewriter_html.insertAfter(ctx.parExpression().getStop(), "</span>");
            }


        }
        if (ctx.FOR() !=null || ctx.WHILE() !=null  || ctx.IF()!=null)
        {
            if(ctx.statement(0).block()== null) // 1 line for,while and if
            {
                count++;
                rewriter.insertBefore(ctx.statement(0).getStart(), code);

                rewriter.insertAfter(ctx.statement(0).getStop(), "\n\t\t//block number "+count+"\n"+"\n\t\t\tw.write(\"block number " + count + " visited\\n\");");
                rewriter.insertAfter(ctx.statement(0).getStop(), "\n\t\t\twcss.write(\"#block_" + count + "{\\n\" +\n" +
                        "                    \"    background-color: #83F492;\\n\" +\n" +//make block id green if the cond. is true
                        "                    \"}\");\n" +
                        "            " + "}");//extremly important; that's how we close the block that we started at #@#
                String ss = "block_" + count;
                rewriter_html.insertBefore(ctx.getStart(), "<div  class=\"global_class\" id=\"" + ss + "\">");
                rewriter_html.insertAfter(ctx.getStop(), "</div>");

            }
            else{  rewriter.insertAfter(ctx.statement(0).getStart(), code);}
        }
    }

    String branch_coverage_check(String s){
        String code="";
        int or_idx = s.lastIndexOf("||");
        if (or_idx != -1) {//true if and only if there is an or
            String first_conditions_befor_or = s.substring(0, or_idx);//26
            String span_id_eq_orange = "\n\t\t\twcss.write(\"#expr_"+count+"{background-color:#F76C3C}\\n\");\n";

            code = "\n\t\t\tif(" + first_conditions_befor_or + "){\n" + span_id_eq_orange + "\n\t\t\t}\n";//for writer that writes in out.java
        }

        return code;
    }

    @Override
    public void enterBlock(JavaParser.BlockContext ctx) {
        count++;
        rewriter.insertAfter(ctx.getStart(), "//block number " + count + "\n");
    }


}