package simlei.graphs
object generic:

  type GenericizableFuns = Function1[?,?] |
                           Function2[?,?,?] |
                           Function3[?,?,?,?] |
                           Function4[?,?,?,?,?] |
                           Function5[?,?,?,?,?,?] |
                           Function6[?,?,?,?,?,?,?] |
                           Function7[?,?,?,?,?,?,?,?] |
                           Function8[?,?,?,?,?,?,?,?,?] |
                           Function9[?,?,?,?,?,?,?,?,?,?] |
                           Function10[?,?,?,?,?,?,?,?,?,?,?] |
                           Function11[?,?,?,?,?,?,?,?,?,?,?,?] |
                           Function12[?,?,?,?,?,?,?,?,?,?,?,?,?] |
                           Function13[?,?,?,?,?,?,?,?,?,?,?,?,?,?] |
                           Function14[?,?,?,?,?,?,?,?,?,?,?,?,?,?,?]
  type GenericizableFunTargets = GenericizableFuns | Function15[?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?]

  type GenericArity[T, F <: GenericizableFuns] <: GenericizableFunTargets = F match
    case Function1[a,z] => Function2[T,a,z]
    case Function2[a,b,z] => Function3[T,a,b,z]
    case Function3[a,b,c,z] => Function4[T,a,b,c,z]
    case Function4[a,b,c,d,z] => Function5[T,a,b,c,d,z]
    case Function5[a,b,c,d,e,z] => Function6[T,a,b,c,d,e,z]
    case Function6[a,b,c,d,e,f,z] => Function7[T,a,b,c,d,e,f,z]
    case Function7[a,b,c,d,e,f,g,z] => Function8[T,a,b,c,d,e,f,g,z]
    case Function8[a,b,c,d,e,f,g,h,z] => Function9[T,a,b,c,d,e,f,g,h,z]
    case Function9[a,b,c,d,e,f,g,h,i,z] => Function10[T,a,b,c,d,e,f,g,h,i,z]
    case Function10[a,b,c,d,e,f,g,h,i,j,z] => Function11[T,a,b,c,d,e,f,g,h,i,j,z]
    case Function11[a,b,c,d,e,f,g,h,i,j,k,z] => Function12[T,a,b,c,d,e,f,g,h,i,j,k,z]
    case Function12[a,b,c,d,e,f,g,h,i,j,k,l,z] => Function13[T,a,b,c,d,e,f,g,h,i,j,k,l,z]
    case Function13[a,b,c,d,e,f,g,h,i,j,k,l,m,z] => Function14[T,a,b,c,d,e,f,g,h,i,j,k,l,m,z]
    case Function14[a,b,c,d,e,f,g,h,i,j,k,l,m,n,z] => Function15[T,a,b,c,d,e,f,g,h,i,j,k,l,m,n,z]
end generic
